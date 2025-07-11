package com.melly.vacationmanager.global.common.exception;

import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice   // 컨트롤러 실행 중 발생하는 예외를 잡음, 모든 요청에서 가로챔
public class GlobalExceptionHandler implements ResponseController {

    // DTO 필드 유효성 검사 실패 시 발생하는 예외를 처리하는 핸들러
    // (@Validated 또는 @Valid 사용 시, @RequestBody DTO 내부 필드 제약조건 위반 시 발생)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        ErrorCode errorCode = resolveErrorCode(fieldError); // ← 여기서 바로 호출

        log.warn("유효성 검증 에러 - Field: {}, Code: {}", fieldError.getField(), fieldError.getCode());

        return makeResponseEntity(
                errorCode.getStatus(),
                errorCode.getErrorCode(),
                errorCode.getMessage(),
                null
        );
    }

    private ErrorCode resolveErrorCode(FieldError error) {
        if (error == null) return ErrorCode.DEFAULT_BAD_REQUEST;

        String field = error.getField();   // 필드 이름
        String code = error.getCode();     // 검증 실패 코드 (NotBlank, Size, Pattern, NotNull 등)

        // username
        if ("username".equals(field)) {
            if ("NotBlank".equals(code)) return ErrorCode.MISSING_USERNAME;
            if ("Size".equals(code)) return ErrorCode.INVALID_LENGTH_USERNAME;
            if ("Pattern".equals(code)) return ErrorCode.INVALID_FORMAT_USERNAME;
        }

        // password
        if ("password".equals(field)) {
            if ("NotBlank".equals(code)) return ErrorCode.MISSING_PASSWORD;
            if ("Size".equals(code)) return ErrorCode.INVALID_LENGTH_PASSWORD;
            if ("Pattern".equals(code)) return ErrorCode.INVALID_FORMAT_PASSWORD;
        }

        // confirmPassword
        if ("confirmPassword".equals(field)) {
            if ("NotBlank".equals(code)) return ErrorCode.MISSING_CONFIRM_PASSWORD;
            // confirmPassword 불일치는 커스텀 validator로 따로 처리해야 하므로 여기선 없음
        }

        // name
        if ("name".equals(field)) {
            if ("NotBlank".equals(code)) return ErrorCode.MISSING_NAME;
            if ("Size".equals(code)) return ErrorCode.INVALID_LENGTH_NAME;
        }

        // email
        if ("email".equals(field)) {
            if ("NotBlank".equals(code)) return ErrorCode.MISSING_EMAIL;
            if ("Email".equals(code)) return ErrorCode.INVALID_FORMAT_EMAIL;
            if ("Size".equals(code)) return ErrorCode.INVALID_LENGTH_EMAIL;
        }

        // hiredate
        if ("hiredate".equals(field)) {
            if ("NotBlank".equals(code)) return ErrorCode.MISSING_HIREDATE;
            // hiredate가 미래일 경우는 커스텀 validator에서 따로 처리해야 함
        }

        // position (Enum)
        if ("position".equals(field)) {
            if ("NotNull".equals(code)) return ErrorCode.MISSING_POSITION;
        }

        return ErrorCode.DEFAULT_BAD_REQUEST;
    }

    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("비즈니스 로직 예외 발생 - Code: {}, Message: {}", errorCode.getErrorCode(), errorCode.getMessage());

        return makeResponseEntity(
                errorCode.getStatus(),
                errorCode.getErrorCode(),
                errorCode.getMessage(),
                null
        );
    }
}
