$(document).ready(function () {
    fetchVacationBalances();

    populateYearOptions();
    populateMonthOptions();

    // 필터 변경 시
    $('#year-select, #month-select').on('change', function () {
        fetchVacationHistory(getFilterParams());
    });

    fetchVacationHistory(getFilterParams());
});

// 휴가 잔여일 데이터 요청
function fetchVacationBalances() {
    $.ajax({
        url: '/api/v1/vacation-balances/me',
        method: 'GET'
    })
        .done(function (response) {
            renderVacationBalanceCards(response.data);
        })
        .fail(function (jqXHR) {
            handleServerError(jqXHR);
        });
}

// 렌더링 함수 분리
function renderVacationBalanceCards(data) {
    const $list = $('#vacation-balance-list');
    $list.empty();

    data.forEach(item => {
        const percentUsed = item.grantedDays > 0
            ? Math.round((item.usedDays / item.grantedDays) * 100)
            : 0;

        const $card = $(`
            <div class="card vacation-card">
                <div class="card-body">
                    <h5 class="card-title">${item.typeName}</h5><hr>                
                    <p><strong>전체 휴가:</strong> ${item.grantedDays}일</p>
                    <p><strong>사용한 휴가:</strong> ${item.usedDays}일</p>
                    <p><strong>잔여 휴가:</strong> ${item.remainingDays}일</p>
                    <div class="progress mt-2">
                        <div class="progress-bar bg-success" role="progressbar"
                             style="width: ${percentUsed}%" aria-valuenow="${percentUsed}"
                             aria-valuemin="0" aria-valuemax="100" title="사용률: ${percentUsed}%">
                            ${percentUsed}%
                        </div>
                    </div>
                    <small class="text-muted">사용률은 부여된 총 휴가 일수 대비 사용한 비율입니다.</small>
                </div>
            </div>
        `);

        $list.append($card);
    });
}

function populateYearOptions() {
    const yearSelect = $('#year-select');
    const thisYear = new Date().getFullYear();
    for (let y = thisYear; y >= 2000; y--) {
        yearSelect.append(`<option value="${y}">${y}</option>`);
    }
}

function populateMonthOptions() {
    const monthSelect = $('#month-select');
    for (let m = 1; m <= 12; m++) {
        monthSelect.append(`<option value="${m}">${m}월</option>`);
    }
}

function getFilterParams() {
    return {
        status: 'APPROVED',  // 고정값
        year: $('#year-select').val(),
        month: $('#month-select').val(),
        dateFilterType: 'vacationPeriod'
    };
}

function fetchVacationHistory(params) {
    $.ajax({
        url: '/api/v1/vacation-requests/me',
        method: 'GET',
        data: params
    })
        .done(function (response) {
            renderVacationHistory(response.data);
        })
        .fail(function (jqXHR) {
            handleServerError(jqXHR);
        });
}

function renderVacationHistory(data) {
    const $list = $('#vacation-history-list');
    $list.empty();

    if (data.content.length === 0) {
        $list.append('<p>조회된 휴가 사용 이력이 없습니다.</p>');
        return;
    }

    data.content.forEach(item => {
        let period = item.startDate;
        if (item.endDate && item.endDate !== item.startDate) {
            period += ` ~ ${item.endDate}`;
        }

        const $item = $(`
            <div>${item.createdAt.slice(0, 10)}</div>
            <div>${getVacationTypeText(item.typeCode)}</div>
            <div>${period}</div>
            <div>${item.daysCount}일</div>
        `);

        $list.append($item);
    });
}

function getVacationTypeText(code) {
    switch (code) {
        case 'ANNUAL': return '연차';
        case 'SICK': return '병가';
        case 'FAMILY_EVENT': return '경조사';
        case 'SPECIAL': return '특별휴가';
        default: return code;
    }
}