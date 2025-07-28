$(document).ready(function () {
    // 초기 필터 옵션 채우기
    populateYearOptions();
    populateMonthOptions();

    fetchVacationList();

    // 필터 변경 시 자동 조회
    $('#name-input').on('input', function () {
        fetchVacationList(getFilterParams());
    });
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

    $(document).on('click', '.process-btn', function () {
        const requestId = $(this).data('request-id');
        const currentStatus = $(this).data('current-status');

        $('#process-requestId').val(requestId);

        // 상태 select 초기화
        const $statusSelect = $('#process-status');
        $statusSelect.empty();
        $statusSelect.append('<option value="">-- 상태 선택 --</option>');

        if (currentStatus === 'PENDING') {
            $statusSelect.append('<option value="APPROVED">승인</option>');
            $statusSelect.append('<option value="REJECTED">반려</option>');
            $statusSelect.append('<option value="ON_HOLD">보류</option>');
        } else if (currentStatus === 'ON_HOLD') {
            $statusSelect.append('<option value="APPROVED">승인</option>');
            $statusSelect.append('<option value="REJECTED">반려</option>');
        }

        $('#processModal').modal('show');
    });

    // 상태 변경 확정 버튼 클릭 시
    $('#confirm-status-btn').on('click', function (e) {
        e.preventDefault(); // 폼 제출 막기

        const requestId = $('#process-requestId').val();
        const newStatus = $('#process-status').val();
        if (!newStatus) {
            alert("상태를 선택해주세요.");
            return;
        }

        $.ajax({
            url: `/api/v1/admin/vacation-requests/${requestId}/status`,
            method: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify({ status: newStatus }),
        })
            .done(function(res) {
                alert(res.message || "상태가 변경되었습니다.");
                $('#processModal').modal('hide');
                fetchVacationList(getFilterParams()); // 목록 갱신
            })
            .fail(function(xhr) {
                const err = xhr.responseJSON;
                if (err && err.message) {
                    alert(`오류: ${err.message}`);
                } else {
                    alert("처리 중 문제가 발생했습니다.");
                }
            });
    });

    // 페이지네이션 클릭 이벤트
    $('#pagination').on('click', 'a.page-link', function (e) {
        e.preventDefault();
        const page = $(this).data('page');
        if (page && page > 0) {
            const params = getFilterParams();
            params.page = page;
            fetchVacationList(params);
        }
    });
});

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

function getFilterParams() {
    return {
        year: $('#year-select').val(),
        month: $('#month-select').val(),
        name: $('#name-input').val(),
        status: $('#status-select').val(),
        typeCode: $('#type-select').val(),
        page: 1,
        size: 10,
        order: $('#sort-createdAt')?.data('order') || 'desc'
    };
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

// 실제 데이터 조회 및 렌더 함수
function fetchVacationList(params = {}) {
    $.ajax({
        url: '/api/v1/admin/vacation-requests',
        method: 'GET',
        data: params
    }).done(function (response) {
        const data = response.data;
        renderVacationList(data.content);
        renderPagination(data);
    }).fail(function (jqXHR, textStatus, errorThrown) {
        console.log("조회 실패", jqXHR);
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
            <div class="list-group-item px-0">
                <div class="row align-items-center text-center py-2 border-bottom">
                    <div class="col-md-2">${v.createdAt?.slice(0, 10) || '-'}</div>
                    <div class="col-md-2">${v.name}</div>
                    <div class="col-md-2">${v.startDate} ~ ${v.endDate}</div>
                    <div class="col-md-1">${getVacationTypeText(v.typeCode)}</div>
                    <div class="col-md-1">${v.daysCount}일</div>
                    <div class="col-md-1">${getStatusBadge(v.status)}</div>
                    <div class="col-md-2">
                        <button class="btn btn-sm btn-outline-secondary reason-btn"
                                data-reason="${v.reason || ''}"
                                data-request-id="${v.requestId}">
                            보기
                        </button>
                    </div>
                    <div class="col-md-1">
                        ${['PENDING', 'ON_HOLD'].includes(v.status)
                                ? `<button class="btn btn-sm btn-outline-primary process-btn"
                           data-request-id="${v.requestId}" 
                           data-current-status="${v.status}">처리</button>`
                                : ''}
                    </div>
                </div>
            </div>
        `;
        list.append(row);
    });
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