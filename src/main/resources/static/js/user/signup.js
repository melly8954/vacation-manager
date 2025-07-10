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
            name: name,
            email: email,
            hireDate: hireDate,
            position: position,
        })
    }).done(function (response) {
        console.log(response.data);
            alert("사용자 가입에 성공했습니다.");
            window.location.href = "/";
    }).fail(function (jqXHR) {
        // jqXHR: jQuery XHR 객체 (응답 전체)
        handleServerError(jqXHR);
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
    if (jqXHR.responseJSON) {
        console.log(jqXHR.responseJSON.code);
        console.log(jqXHR.responseJSON.error_code);
        console.log(jqXHR.responseJSON.message);
        console.log(jqXHR.responseJSON.data);
    //     const { error_code, message } = jqXHR.responseJSON;
    //     alert("회원가입 실패: " + message);
    //
    //     switch (error_code) {
    //         case "missing_username":
    //         case "invalid_length_username":
    //         case "invalid_format_username":
    //         case "duplicate_username":
    //             $("#username").focus();
    //             break;
    //         case "missing_password":
    //         case "invalid_length_password":
    //         case "invalid_format_password":
    //             $("#password").focus();
    //             break;
    //         case "missing_name":
    //         case "invalid_length_name":
    //             $("#name").focus();
    //             break;
    //         case "missing_email":
    //         case "invalid_length_email":
    //         case "invalid_format_email":
    //         case "duplicate_email":
    //             $("#email").focus();
    //             break;
    //         case "missing_hiredate":
    //         case "invalid_format_hiredate":
    //             $("#hireDate").focus();
    //             break;
    //         case "missing_position":
    //             $("#position").focus();
    //             break;
    //         default:
    //             // 기타 처리, 포커스 필요없으면 그냥 무시 가능
    //             break;
    //     }
    // } else {
    //     alert("회원가입 실패: 알 수 없는 오류가 발생했습니다.");
    }
}