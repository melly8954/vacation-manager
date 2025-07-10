package com.melly.vacationmanager.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // username
    MISSING_USERNAME("missing_username", "username은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_USERNAME("invalid_length_username", "username은 8자 이상 20자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_USERNAME("invalid_format_username", "username은 영문, 숫자만 허용합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_USERNAME("duplicate_username", "이미 사용 중인 username입니다.", HttpStatus.CONFLICT),

    // password
    MISSING_PASSWORD("missing_password", "password는 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_PASSWORD("invalid_length_password", "password는 8자 이상 20자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_PASSWORD("invalid_format_password", "password는 소문자, 숫자, 특수문자를 포함해야 하며 특수문자로 시작할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("password_mismatch", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    MISSING_CONFIRM_PASSWORD("missing_confirm_password", "비밀번호 확인을 입력하세요.", HttpStatus.BAD_REQUEST),

    // name
    MISSING_NAME("missing_name", "name은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_NAME("invalid_length_name", "name은 2자 이상 10자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),

    // email
    MISSING_EMAIL("missing_email", "email은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_EMAIL("invalid_length_email", "email은 50자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_EMAIL("invalid_format_email", "올바른 이메일 형식을 입력하세요.", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL("duplicate_email", "이미 사용 중인 email입니다.", HttpStatus.CONFLICT),

    // hiredate
    MISSING_HIREDATE("missing_hiredate", "hiredate는 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_HIREDATE("invalid_format_hiredate", "입사일은 오늘 이후 날짜일 수 없습니다.", HttpStatus.BAD_REQUEST),

    // position
    MISSING_POSITION("missing_position", "position은 필수 선택 항목입니다.", HttpStatus.BAD_REQUEST),

    // 공통
    INTERNAL_ERROR("internal_error", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DEFAULT_BAD_REQUEST("bad_request", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST);

    private final String errorCode;      // error_code
    private final String message;       // 사용자 메시지
    private final HttpStatus status;        // HTTP 상태
}
