$(document).ready(function () {
    // 초기 필터 옵션 채우기
    populateYearOptions();
    populateMonthOptions();

    // 오늘 날짜 연도, 월 변수
    const today = new Date();
    const thisYear = today.getFullYear();
    const thisMonth = today.getMonth() + 1; // JS 월은 0~11

    // 오늘 날짜에 맞춰 옵션 선택
    $('#year-select').val(thisYear);
    $('#month-select').val(thisMonth);

    $('#year-select, #month-select').on('change', function() {
        const year = $('#year-select').val();
        const month = $('#month-select').val();
        fetchGrantStatistics(year);
        fetchUsageStatistics(year, month)
    });

    // 오늘 날짜 기준으로 통계 조회 함수 호출
    fetchGrantStatistics(thisYear);
    fetchUsageStatistics(thisYear,thisMonth)

});

let usageBarChartInstance = null;
const defaultVacationTypes = [
    '연차',
    '병가',
    '경조사',
    '특별휴가'
];

// 연도, 월 옵션 채우기
function populateYearOptions() {
    const yearSelect = $('#year-select');
    const thisYear = new Date().getFullYear();
    for(let y=thisYear; y>=2000; y--) {
        yearSelect.append(`<option value="${y}">${y}</option>`);
    }
}
function populateMonthOptions() {
    const monthSelect = $('#month-select');
    for(let m=1; m<=12; m++) {
        monthSelect.append(`<option value="${m}">${m}월</option>`);
    }
}

function fetchGrantStatistics(year) {
    $.ajax({
        url: '/api/v1/admin/vacation-statistics/grants',
        method: 'GET',
        data: {
            year: year,
        }
    })
        .done(function(response) {
            renderGrantCards(response.data);
        })
        .fail(function() {
            alert('통계 데이터를 불러오는데 실패했습니다.');
        });
}

function renderGrantCards(data) {
    const container = $('#grant-stats-cards');
    container.empty();

    container.append(`
                <p class="section-label">휴가 총 지급일 수</p>
                <p class="text-muted small mt-2" style="margin-top:-10px; margin-bottom: 20px;">
                ※ 휴가 지급 통계는 <strong>연 단위 기준</strong>으로 제공됩니다. 월 선택은 무시됩니다.</p>                    
        `);

    // 응답 데이터를 Map 형태로 변환 (typeName → totalGrantedDays)
    const grantMap = new Map();
    if (Array.isArray(data)) {
        data.forEach(item => {
            grantMap.set(item.typeName, item.totalGrantedDays);
        });
    }

    defaultVacationTypes.forEach(typeName => {
        const grantedDays = grantMap.get(typeName) || 0;

        container.append(`
            <div class="col-md-3 col-sm-6 mb-4">
                <div class="stat-card--grant">
                    <div class="stat-title--grant">${typeName}</div>
                    <div class="stat-value--grant">${grantedDays} 일</div>
                </div>
            </div>
        `);
    });
}

function fetchUsageStatistics(year,month) {
    $.ajax({
        url: '/api/v1/admin/vacation-statistics/usages',
        method: 'GET',
        data: { year: year }
    })
        .done(function(response) {
            renderUsageCards(response.data, Number(month));
            renderUsageBarChart(response.data);
        })
        .fail(function() {
            alert('휴가 사용 통계 데이터를 불러오는데 실패했습니다.');
        });
}

function renderUsageCards(data, month) {
    const container = $('#usage-stats-cards');
    container.empty();
    container.append(`
          <p class="section-label">휴가 총 사용일 수</p>
          <p class="text-muted small mt-2" style="margin-top:-10px; margin-bottom: 20px;">
            ※ 휴가 사용 통계는 <strong>휴가 시작일 기준</strong>으로 집계되며, 시작 월에 전부 포함됩니다.<br />
            예: 1/31 ~ 2/2 → 1월로 포함
          </p>
    `);

    const filtered = data.filter(item => item.month === month);
    const usageMap = new Map();

    filtered.forEach(item => {
        usageMap.set(item.typeName, item.totalUsedDays);
    });

    defaultVacationTypes.forEach(typeName => {
        const usedDays = usageMap.get(typeName) || 0;

        container.append(`
            <div class="col-md-3 col-sm-6 mb-4">
                <div class="stat-card--usage">
                    <div class="stat-title--usage">${typeName}</div>
                    <div class="stat-value--usage">${usedDays} 일</div>
                </div>
            </div>
        `);
    });
}

function renderUsageBarChart(data) {
    const ctx = document.getElementById('usage-bar-chart').getContext('2d');

    // 월별 + 유형별로 데이터 재구성
    const chartData = {};
    defaultVacationTypes.forEach(type => {
        chartData[type] = Array(12).fill(0); // 1~12월
    });

    data.forEach(item => {
        const monthIdx = item.month - 1; // 0~11
        chartData[item.typeName][monthIdx] = item.totalUsedDays;
    });

    const backgroundColors = {
        '연차': '#198754',
        '병가': '#fd7e14',
        '경조사': '#6c757d',
        '특별휴가': '#6f42c1'
    };

    const datasets = defaultVacationTypes.map(type => ({
        label: type,
        data: chartData[type],
        backgroundColor: backgroundColors[type],
    }));

    // 기존 차트 제거
    if (usageBarChartInstance) {
        usageBarChartInstance.destroy();
    }
    usageBarChartInstance = new Chart(ctx, {

        type: 'bar',
        data: {
            labels: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
            datasets: datasets
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: false
                },
                tooltip: {
                    mode: 'index',
                    intersect: false
                }
            },
            interaction: {
                mode: 'nearest',
                axis: 'x',
                intersect: false
            },
            scales: {
                x: {
                    stacked: false  // 기존: true
                },
                y: {
                    stacked: false, // 기존: true
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: '사용일 수'
                    },
                    ticks: {
                        stepSize: 5
                    }
                }
            }
        }
    });
}