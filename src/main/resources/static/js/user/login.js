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
        setTimeout(() => {
            window.location.href = "/";
        }, 100);  // 쿠키 반영 시간 확보
    }).fail(function (jqXHR, textStatus, errorThrown) {
        handleServerError(jqXHR);
    });
}

