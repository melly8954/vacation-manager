$(document).ready(function () {
    fetchVacationBalances();
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
        .fail(function () {
            alert('휴가 잔여일 정보를 가져오지 못했습니다.');
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