package com.melly.vacationmanager.global.common.exception;

import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice   // 컨트롤러 실행 중 발생하는 예외를 잡음, 모든 요청에서 가로챔
public class GlobalExceptionHandler implements ResponseController {

    // DTO 필드 유효성 검사 실패 시 발생하는 예외를 처리하는 핸들러
    // (@Validated 또는 @Valid 사용 시, @RequestBody DTO 내부 필드 제약조건 위반 시 발생)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 첫 번째 필드 에러 메시지를 가져옴
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");

        // 전체 필드 에러를 로깅
        ex.getBindingResult().getFieldErrors().forEach(error ->
                log.warn("유효성 검증 에러 - field: {}, message: {}", error.getField(), errorMessage)
        );

        // 응답 생성
        return makeResponseEntity(
                HttpStatus.BAD_REQUEST,
                ErrorCode.DEFAULT_BAD_REQUEST.getErrorCode(),
                errorMessage,
                null
        );
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleException(Exception e) {
        log.error("500 Error : " + e.getMessage());
        return makeResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,ErrorCode.INTERNAL_ERROR.getErrorCode(),"서버 내부 오류가 발생했습니다.", null);
    }
}
