$(document).ready(function() {
    let nameTimer;

    populateYearOptions();
    populateMonthOptions();

    // 최초 목록 로딩
    fetchPendingUsers();

    // 실시간 이름 검색
    $('#name').on('input', function () {
        clearTimeout(nameTimer);
        nameTimer = setTimeout(() => {
            fetchPendingUsers(getFilterParams());
        }, 300); // 300ms debounce
    });

    // 실시간 연도/월 필터링
    $('#year, #month').on('change', function () {
        fetchPendingUsers(getFilterParams());
    });

    // 초기화 버튼 클릭 이벤트
    $('#reset-btn').on('click', function () {
        // 입력값 초기화
        $('#name').val('');
        $('#year').val('ALL');
        $('#month').val('ALL');
        // 정렬 순서 초기화 (필요 시)
        currentOrder = 'desc'; // 전역 정렬 변수 초기화
        $('#sort-userId .sort-icon').text('▼');

        // 목록 다시 불러오기
        fetchPendingUsers(getFilterParams());
    });

    // 정렬 헤더 클릭 이벤트
    $('#sort-userId').on('click', function () {
        currentOrder = currentOrder === 'desc' ? 'asc' : 'desc';

        const icon = currentOrder === 'asc' ? '▲' : '▼';
        $(this).find('.sort-icon').text(icon);

        const params = getFilterParams();
        params.order = currentOrder;
        fetchPendingUsers(params);
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
});

// 전역 변수
let currentOrder = 'desc'; // 전역 정렬 상태

// 연도 필터링 동적 처리
function populateYearOptions() {
    const yearSelect = $('#year');
    const currentYear = new Date().getFullYear();
    const startYear = 2000;

    for (let y = currentYear; y >= startYear; y--) {
        yearSelect.append(`<option value="${y}">${y}</option>`);
    }
}

// 월 필터링 동적 처리
function populateMonthOptions() {
    const monthSelect = $('#month');
    for (let m = 1; m <= 12; m++) {
        monthSelect.append(`<option value="${m}">${m}월</option>`);
    }
}

// getFilterParams() 수정
function getFilterParams() {
    return {
        name: $('#name').val(),
        year: Number($('#year').val()),
        month: Number($('#month').val()),
        order: currentOrder, // ← 여기 반영
        page: 1,
        size: 10
    };
}

// 대기 목록 유저 필터링 조회
function fetchPendingUsers(params = {}) {
    // 기본 파라미터 세팅
    const queryParams = {
        page: params.page || 1,
        size: params.size || 10,
        year: params.year || 'ALL',
        month: params.month || 'ALL',
        name: params.name || '',
        order: params.order || 'desc'
    };

    $.ajax({
        url: "/api/v1/admin/users/pending",
        method: "GET",
        data: queryParams
    }).done(function (response) {
       console.log(response);
       renderPendingUsers(response.data.pendingUsers);
       renderPagination(response.data);
    }).fail(function (jqXHR, textStatus, errorThrown) {
        handleServerError(jqXHR);
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
                <div class="user-name">${user.userId}</div>
                <div class="user-name">${user.name}</div>
                <div class="user-email">${user.email}</div>
                <div class="user-position">${user.position}</div>
                <div class="user-createdAt">${user.createdAt.slice(0,10)}</div>
                <div class="user-actions">
                    <button class="btn btn-success btn-sm" data-user-id="${user.userId}" onclick="processUserStatus(this, 'approved')">승인</button>
                    <button class="btn btn-danger btn-sm" data-user-id="${user.userId}" onclick="processUserStatus(this, 'rejected')">반려</button>
                </div>
            </li>
        `;
        list.append(item);
    });
}

// 승인 대기 처리
function processUserStatus(button, status){
    const userId = button.getAttribute('data-user-id');

    // 상태에 따라 확인 메시지 설정
    let message = '';
    switch (status) {
        case 'approved':
            message = '정말로 이 사용자를 승인하시겠습니까?';
            break;
        case 'rejected':
            message = '정말로 이 사용자를 반려하시겠습니까?';
            break;
    }
    // 확인창
    if (!confirm(message)) {
        return; // 사용자가 취소하면 아무것도 하지 않음
    }

    $.ajax({
        url: `/api/v1/admin/users/${userId}/status`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({ status: status })
    }).done(function (response) {
        console.log(response);
        alert("변경 완료");
        fetchPendingUsers(getFilterParams());
    }).fail(function (jqXHR, textStatus, errorThrown) {
        handleServerError(jqXHR);
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