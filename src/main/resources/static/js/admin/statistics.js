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
        fetchUsageStatistics(thisYear,month)
    });

    // 오늘 날짜 기준으로 통계 조회 함수 호출
    fetchGrantStatistics(thisYear);
    fetchUsageStatistics(thisYear,thisMonth)

});

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
            console.log(response);
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

function fetchUsageStatistics(year, month) {
    $.ajax({
        url: '/api/v1/admin/vacation-statistics/usages',
        method: 'GET',
        data: { year: year, month: month }
    })
        .done(function(response) {
            console.log('사용 통계:', response);
            renderUsageCards(response.data);
        })
        .fail(function() {
            alert('휴가 사용 통계 데이터를 불러오는데 실패했습니다.');
        });
}

function renderUsageCards(data) {
    const container = $('#usage-stats-cards');
    container.empty();
    container.append(`
          <p class="section-label">휴가 총 사용일 수</p>
          <p class="text-muted small mt-2" style="margin-top:-10px; margin-bottom: 20px;">
            ※ 휴가 사용 통계는 <strong>휴가 시작일 기준</strong>으로 집계되며, 시작 월에 전부 포함됩니다.<br />
            예: 1/31 ~ 2/2 → 1월로 포함
          </p>
    `);

    // 응답 데이터를 Map 형태로 변환 (typeName → totalUsedDays)
    const usageMap = new Map();
    if (Array.isArray(data)) {
        data.forEach(item => {
            usageMap.set(item.typeName, item.totalUsedDays);
        });
    }

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