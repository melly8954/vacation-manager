package com.melly.vacationmanager.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// enum 상수 선언부는 클래스 내 가장 앞에 위치해야 컴파일 오류가 발생하지 않는다.
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // username
    MISSING_USERNAME("missing_username", "아이디는 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_USERNAME("invalid_length_username", "아이디는 8자 이상 20자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_USERNAME("invalid_format_username", "아이디는 영문, 숫자만 허용합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_USERNAME("duplicate_username", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),

    // password
    MISSING_PASSWORD("missing_password", "비밀번호는 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_PASSWORD("invalid_length_password", "비밀번호는 8자 이상 20자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_PASSWORD("invalid_format_password", "비밀번호는 소문자, 숫자, 특수문자를 포함해야 하며 특수문자로 시작할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("password_mismatch", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    MISSING_CONFIRM_PASSWORD("missing_confirm_password", "비밀번호 확인을 입력하세요.", HttpStatus.BAD_REQUEST),

    // name
    MISSING_NAME("missing_name", "이름은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_NAME("invalid_length_name", "이름은 2자 이상 10자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),

    // email
    MISSING_EMAIL("missing_email", "이메일은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LENGTH_EMAIL("invalid_length_email", "이메일은 50자 이하로 입력하세요.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_EMAIL("invalid_format_email", "올바른 이메일 형식을 입력하세요.", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL("duplicate_email", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),

    // hiredate
    MISSING_HIREDATE("missing_hiredate", "입사일은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT_HIREDATE("invalid_format_hiredate", "입사일은 오늘 이후 날짜일 수 없습니다.", HttpStatus.BAD_REQUEST),

    // position
    MISSING_POSITION("missing_position", "직급은 필수 선택 항목입니다.", HttpStatus.BAD_REQUEST),

    // 중복 체크 유형
    INVALID_TYPE_VALUE("invalid_type_value", "지원하지 않는 중복 검사 타입입니다.", HttpStatus.BAD_REQUEST),

    // 존재하지 않는 유저
    USER_NOT_FOUND("user_not_found", "해당 정보로 등록된 사용자가 없습니다.", HttpStatus.NOT_FOUND),

    // 유효하지 않은 상태 요청
    INVALID_STATUS("invalid_status","유효하지 않은 상태입니다.", HttpStatus.BAD_REQUEST),

    // 존재하지 않는 휴가타입
    VACATION_TYPE_NOT_FOUND("vacation_type_not_found","존재하지 않는 휴가 타입입니다.",HttpStatus.NOT_FOUND),

    // 휴가 신청 시 에러코드
    INVALID_DATE_RANGE("invalid_date_range", "시작일은 종료일보다 이전이어야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_DAYS_COUNT("invalid_days_count", "휴가 일수는 0일 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    VACATION_BALANCE_NOT_FOUND("vacation_balance_not_found","휴가 잔여일이 존재하지 않습니다.",HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE("insufficient_balance", "해당 유형의 잔여 휴가 일수가 부족합니다.", HttpStatus.BAD_REQUEST),
    OVERLAPPING_APPROVED_VACATION("overlapping_approved_vacation", "이미 승인된 휴가 일정과 중복됩니다.", HttpStatus.CONFLICT),
    VACATION_REQUEST_NOT_FOUND("vacation_request_not_found", "해당 휴가 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 파일 관련
    FILE_NOT_FOUND("file_not_found", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED("file_upload_failed", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("file_size_exceeded", "파일 크기가 허용된 범위를 초과했습니다.", HttpStatus.BAD_REQUEST),

    // 공통
    INTERNAL_ERROR("internal_error", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DEFAULT_BAD_REQUEST("bad_request", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST);

    private final String errorCode;      // error_code
    private final String message;       // 사용자 메시지
    private final HttpStatus status;        // HTTP 상태
}
