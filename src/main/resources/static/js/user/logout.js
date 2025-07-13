function logout() {
    $.ajax({
        url: "/api/v1/auth/logout",
        method: "POST"
    }).done(() => {
        window.location.href = "/"; // 로그아웃 후 홈으로 이동
    }).fail(() => {
        alert("로그아웃에 실패했습니다.");
    });
}