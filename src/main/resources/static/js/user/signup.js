function signup() {
    const username = $("#username").val().trim();
    const password = $("#password").val();
    const confirmPassword = $("#confirmPassword").val();
    const name = $("#name").val().trim();
    const email = $("#email").val().trim();
    const hireDate = $("#hireDate").val();
    const position = $("#position").val();

    // 필드 유효성 검사
    if (!validateForm(username, password, confirmPassword, name, email, hireDate, position)) return;

    $.ajax({
        url: "/api/v1/users",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            username: username,
            password: password,
            confirmPassword: confirmPassword,
            name: name,
            email: email,
            hireDate: hireDate,
            position: position,
        })
    }).done(function (response) {
        console.log(response.data);
            alert("사용자 가입에 성공했습니다.");
            window.location.href = "/";
    }).fail(function (jqXHR, textStatus, errorThrown) {
        // jqXHR: jQuery XHR 객체 (응답 전체)
        handleServerError(jqXHR, textStatus, errorThrown);
    });
}

function validateForm(username, password, confirmPassword, name, email, hireDate, position){
    if (!username) {
        alert("아이디를 입력하세요.");
        return false;
    }
    if (!password) {
        alert("비밀번호를 입력하세요.");
        return false;
    }
    if (!confirmPassword) {
        alert("비밀번호 확인을 입력하세요.");
        return false;
    }
    if (!name) {
        alert("이름을 입력하세요.");
        return false;
    }
    if (!email) {
        alert("이메일을 입력하세요.");
        return false;
    }
    if (!hireDate) {
        alert("입사일을 선택하세요.");
        return false;
    }
    if (!position) {
        alert("직급을 선택하세요.");
        return false;
    }
    return true;
}

function handleServerError(jqXHR) {
    console.log(jqXHR);
    // 서버에서 내려준 메시지 활용 (JSON 응답인 경우)
    if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
        alert(jqXHR.responseJSON.message);
    }

}