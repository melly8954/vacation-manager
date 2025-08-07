$(document).ready(function () {
    populateYearOptions();
    populateMonthOptions();
    populateVacationTypeOptions();

    // 최초 목록 조회
    fetchVacationList();

    // 필터 변경 시 자동 조회
    $('#year-select, #month-select, #status-select, #type-select').on('change', function () {
        fetchVacationList(getFilterParams());
    });

    // 신청일 정렬 버튼 클릭 이벤트
    $('#sort-createdAt').on('click keypress', function(e) {
        // 키보드 접근성 위해 keypress 처리 (Enter or Space)
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();

            const $this = $(this);
            const currentOrder = $this.data('order');
            const newOrder = currentOrder === 'desc' ? 'asc' : 'desc';

            $this.data('order', newOrder);
            $this.find('.sort-icon').text(newOrder === 'desc' ? '▼' : '▲');

            // 정렬 순서 업데이트 후 재조회
            fetchVacationList(getFilterParams());
        }
    });

    // 페이지네이션 클릭 이벤트 (위임)
    $('#pagination').on('click', 'a.page-link', function (e) {
        e.preventDefault();
        const page = $(this).data('page');
        if (page && page > 0) {
            const params = getFilterParams();
            params.page = page;
            fetchVacationList(params);
        }
    });

    // 신청 사유 보기 버튼 클릭 이벤트
    $(document).on('click', '.reason-btn', function () {
        const reason = $(this).data('reason') || '사유 없음';
        const requestId = $(this).data('requestId');

        const modalBody = $('#reasonModalBody');
        modalBody.html(`<p><strong>사유:</strong> ${reason}</p>`);

        const modal = new bootstrap.Modal(document.getElementById('reasonModal'));
        modal.show();

        fetchEvidenceFiles(requestId, '#reasonModalBody');
    });

    // 취소 버튼 클릭 이벤트
    $(document).on('click', '.cancel-btn', function () {
        const requestId = $(this).data('requestId');
        if (confirm('해당 휴가 신청을 정말 취소하시겠습니까?')) {
            $.ajax({
                url: `/api/v1/vacation-requests/${requestId}/status`,
                method: 'PATCH',
            })
                .done(function(response) {
                    alert(response.message);
                    fetchVacationList(getFilterParams());
                })
                .fail(function(jqXHR) {
                    handleServerError(jqXHR);
                });
        }
    });
});

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

function populateVacationTypeOptions() {
    const vacationType = $('#type-select');
    vacationType.empty(); // 기존 옵션 제거

    $.getJSON('/api/v1/vacation-types')
        .done(function(response) {
            vacationType.append('<option value="ALL" selected>전체 휴가 유형</option>');
            response.data.types.forEach(function(type) {
                vacationType.append(`<option value="${type.typeCode}">${type.typeName}</option>`);
            });
        })
        .fail(function(jqXHR) {
            handleServerError(jqXHR);
        });
}

function getFilterParams() {
    return {
        year: $('#year-select').val(),
        month: $('#month-select').val(),
        status: $('#status-select').val(),
        'type-code': $('#type-select').val(),
        page: 1,
        size: 10,
        order: $('#sort-createdAt').data('order'),
        dateFilterType: 'createdAt'
    };
}

function fetchVacationList(params = {}) {
    $.ajax({
        url: '/api/v1/vacation-requests/me',
        method: 'GET',
        data: params
    }).done(function (response) {
        const data = response.data;
        renderVacationList(data.content);
        renderPagination(data);
    }).fail(function (jqXHR, textStatus, errorThrown) {
        handleServerError(jqXHR);
    });
}

function renderVacationList(items) {
    const list = $('#vacation-list');
    list.empty();

    if (!items || items.length === 0) {
        list.append('<div class="text-center py-3">휴가 신청 내역이 없습니다.</div>');
        return;
    }

    items.forEach(v => {
        const row = `
            <div class="row border-bottom py-2 text-center">
                <div class="col-md-2">${v.createdAt.slice(0, 10)}</div>
                <div class="col-md-2">${v.startDate} ~ ${v.endDate}</div>
                <div class="col-md-2">${getVacationTypeText(v.typeCode)}</div>
                <div class="col-md-2">${v.daysCount}일</div>
                <div class="col-md-2">${getStatusBadge(v.status)}</div>
                <div class="col-md-1">
                    <button class="btn btn-sm btn-outline-secondary reason-btn" 
                            data-reason="${v.reason || ''}"
                            data-request-id="${v.requestId}">
                        보기
                    </button>
                </div>
                <div class="col-md-1">
                    ${v.status === 'PENDING' ? `<button class="btn btn-sm btn-outline-danger cancel-btn" data-request-id="${v.requestId}">취소</button>` : '-'}
                </div>
            </div>
        `;
        list.append(row);
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

function getStatusBadge(status) {
    let badgeClass = 'secondary';
    switch (status) {
        case 'PENDING': badgeClass = 'warning'; break;
        case 'APPROVED': badgeClass = 'success'; break;
        case 'REJECTED': badgeClass = 'danger'; break;
        case 'ON_HOLD': badgeClass = 'info'; break;
        case 'CANCELED': badgeClass = 'dark'; break;
    }
    return `<span class="badge bg-${badgeClass}">${status}</span>`;
}

function fetchEvidenceFiles(requestId, containerSelector) {
    $.ajax({
        url: `/api/v1/vacation-requests/${requestId}/evidence-files`,
        method: 'GET',
        success: function(response) {
            const files = response.data;
            if (!files || files.length === 0) {
                $(containerSelector).append('<p>증빙자료가 없습니다.</p>');
            } else {
                let fileListHtml = '<p>증빙 자료</p><ul>';
                files.forEach(file => {
                    fileListHtml += `<li><a href="${file.downloadUrl}" target="_blank">${file.originalName}</a></li>`;
                });
                fileListHtml += '</ul>';
                $(containerSelector).append(fileListHtml);
            }
        },
        error: function() {
            $(containerSelector).append('<p class="text-danger">증빙자료를 불러오는 데 실패했습니다.</p>');
        }
    });
}

function renderPagination(data) {
    const pagination = $('#pagination');
    pagination.empty();

    const current = data.page;
    const total = data.totalPages;

    if (total <= 1) return;

    pagination.append(`
        <li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current - 1}">이전</a>
        </li>
    `);

    for (let i = 1; i <= total; i++) {
        pagination.append(`
            <li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>
        `);
    }

    pagination.append(`
        <li class="page-item ${current === total ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current + 1}">다음</a>
        </li>
    `);
}