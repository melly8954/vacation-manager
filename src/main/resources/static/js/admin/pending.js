$(document).ready(function() {
    populateYearOptions();
    populateMonthOptions();

    // 최초 목록 로딩
    fetchPendingUsers();

    // 검색 버튼 클릭 이벤트
    $('#search-btn').on('click', function () {
        fetchPendingUsers(getFilterParams());
    });

    // 페이지네이션 클릭 이벤트 (이벤트 위임)
    $('#pagination').on('click', 'a.page-link', function (e) {
        e.preventDefault();
        const page = $(this).data('page');
        if (page && page > 0) {
            const params = getFilterParams();
            params.page = page;
            fetchPendingUsers(params);
        }
    });

    // 필터에서 값 가져오는 함수
    function getFilterParams() {
        return {
            name: $('#name').val(),
            year: Number($('#year').val()),
            month: Number($('#month').val()),
            order: $('#order').val(),
            page: 1,
            size: 10
        };
    }
});

// 연도 필터링 동적 처리
function populateYearOptions() {
    const yearSelect = $('#year');
    const currentYear = new Date().getFullYear();
    const startYear = 2000;

    yearSelect.append(`<option value="">전체</option>`);
    for (let y = currentYear; y >= startYear; y--) {
        yearSelect.append(`<option value="${y}">${y}</option>`);
    }
}

// 월 필터링 동적 처리
function populateMonthOptions() {
    const monthSelect = $('#month');
    monthSelect.append(`<option value="">전체</option>`);
    for (let m = 1; m <= 12; m++) {
        monthSelect.append(`<option value="${m}">${m}월</option>`);
    }
}

// 대기 목록 유저 필터링 조회
function fetchPendingUsers(params = {}) {
    // 기본 파라미터 세팅
    const queryParams = {
        page: params.page || 1,
        size: params.size || 10,
        year: params.year || '',
        month: params.month || '',
        name: params.name || '',
        order: params.order || 'desc'
    };

    $.ajax({
        url: "/api/v1/admin/users/pending",
        method: "GET",
        dataType: "json",
        data: queryParams
    }).done(function (response) {
       console.log(response);
       renderPendingUsers(response.data.content);
       renderPagination(response.data);
    }).fail(function (jqXHR, textStatus, errorThrown) {

    });
}

// 조회된 유저 동적 렌더링
function renderPendingUsers(data) {
    const list = $('#pending-user-list');
    list.empty();

    if (!data || data.length === 0) {
        list.append('<li class="list-group-item text-center">조회된 사용자가 없습니다.</li>');
        return;
    }

    data.forEach(user => {
        const item = `
            <li class="list-group-item user-list-item">
                <div class="user-name">${user.name}</div>
                <div class="user-email">${user.email}</div>
                <div class="user-position">${user.position}</div>
                <div class="user-createdAt">${user.createdAt.slice(0,10)}</div>
                <div class="user-actions">
                    <button class="btn btn-success btn-sm approve-btn" data-user-id="${user.userId}">승인</button>
                    <button class="btn btn-danger btn-sm reject-btn" data-user-id="${user.userId}">반려</button>
                </div>
            </li>
        `;
        list.append(item);
    });
}

// 페이지네이션
function renderPagination(data) {
    const pagination = $('#pagination');
    pagination.empty();

    const currentPage = data.page;
    const totalPages = data.totalPages;

    if (totalPages <= 1) return; // 페이지 1개면 생략

    // 이전 버튼
    pagination.append(`
        <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">이전</a>
        </li>
    `);

    // 페이지 번호
    for (let i = 1; i <= totalPages; i++) {
        pagination.append(`
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>
        `);
    }

    // 다음 버튼
    pagination.append(`
        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">다음</a>
        </li>
    `);
}