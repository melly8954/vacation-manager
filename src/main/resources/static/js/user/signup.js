// 아이디 중복 검사 통과 여부 상태
let isUsernameAvailable = false;

// DOM 완전히 로드되고 나서 JavaScript 코드를 실행하도록 보장하는 jQuery 구문
$(document).ready(function() {
    // 아이디 입력 값이 바뀌면 중복 검사 상태 초기화
    $("#username").on("input", function() {
        isUsernameAvailable = false;
        $("#username-check-msg").removeClass("msg-success msg-error").text("");
    });
});

// 사용자 가입
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
    // 아이디 중복 검사 통과 여부
    if (!isUsernameAvailable) {
        alert("아이디 중복 확인이 필요합니다.");
        $("#username").focus();
        return;
    }
    
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
        handleServerError(jqXHR);
    });
}

// 가입 폼 유효성 검사
function validateForm(username, password, confirmPassword, name, email, hireDate, position){
    if (!username) {
        alert("아이디를 입력하세요.");
        $("#username").focus();
        return false;
    }
    if (!password) {
        alert("비밀번호를 입력하세요.");
        $("#password").focus();
        return false;
    }
    if (!confirmPassword) {
        alert("비밀번호 확인을 입력하세요.");
        $("#confirmPassword").focus();
        return false;
    }
    if (!name) {
        alert("이름을 입력하세요.");
        $("#name").focus();
        return false;
    }
    if (!email) {
        alert("이메일을 입력하세요.");
        $("#email").focus();
        return false;
    }
    if (!hireDate) {
        alert("입사일을 선택하세요.");
        $("#hireDate").focus();
        return false;
    }
    if (!position) {
        alert("직급을 선택하세요.");
        $("#position").focus();
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

// 필드 중복 검사
function duplicateCheck(e){   // 버튼 자신을 인자로 넘김
    const type = $(e).data("type");
    const targetSelector = $(e).data("target");  // 해당 버튼이 참조할 태그의 id 값을 암시
    const value = $(targetSelector).val().trim();
    const messageSpan = $("#username-check-msg");

    if (!value) {
        alert("아이디를 입력하세요.")
        $("#username").focus();
        return false;
    }

    $.ajax({
        url: "/api/v1/users/duplicate-check?type="+type + "&value="+value,
        method: "GET",
    }).done(function (response) {
        console.log(response.data);
        isUsernameAvailable = true;
        messageSpan.removeClass("msg-error")
            .addClass("msg-success")
            .text("사용 가능한 아이디입니다.");
    }).fail(function (jqXHR, textStatus, errorThrown) {
        // jqXHR: jQuery XHR 객체 (응답 전체)
        handleServerError(jqXHR);
        // 서버에서 메시지 내려온 경우
        let errorMessage = "";
        if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
            errorMessage = jqXHR.responseJSON.message;
        }
        messageSpan
            .removeClass("msg-success")
            .addClass("msg-error")
            .text(errorMessage);
    });
}