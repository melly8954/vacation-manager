function login(){
    const username= $('#login-username').val();
    const password= $('#login-password').val();

    $.ajax({
        url: "/api/v1/auth/login",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            username: username,
            password: password,
        })
    }).done(function (response) {
        alert(response.message);
        window.location.href = "/";  // 홈으로 리디렉션
    }).fail(function (jqXHR, textStatus, errorThrown) {
        handleServerError(jqXHR);
    });
}

function handleServerError(jqXHR) {
    console.log(jqXHR);
    // 서버에서 내려준 메시지 활용 (JSON 응답인 경우)
    if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
        alert(jqXHR.responseJSON.message);
    }
}