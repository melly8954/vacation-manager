package com.melly.vacationmanager.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400
    MISSING_USERNAME("missing_username", "username은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_USERNAME("invalid_length_username", "username은 8자 이상 20자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("password_mismatch", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 409
    DUPLICATE_USERNAME("duplicate_username", "이미 사용 중인 username입니다.", HttpStatus.CONFLICT),

    // 공통
    INTERNAL_ERROR("internal_error", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;      // error_code
    private final String message;       // 사용자 메시지
    private final HttpStatus status;        // HTTP 상태
}
