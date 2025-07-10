package com.melly.vacationmanager.global.common.controller;

import com.melly.vacationmanager.global.common.dto.ResponseDto;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// JSON API 공통응답
public interface ResponseController {
    default ResponseEntity<ResponseDto> makeResponseEntity(HttpStatus httpStatus, String errorCode, String message, Object data) {
        // JSON 응답 바디 구조
        ResponseDto responseDto = ResponseDto.builder()
                .code(httpStatus.value())
                .errorCode(errorCode)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(httpStatus).body(responseDto);
    }
}
