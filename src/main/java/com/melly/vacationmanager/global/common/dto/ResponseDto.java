package com.melly.vacationmanager.global.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private int code;

    @JsonProperty("error_code")
    private String errorCode;

    private String message;
    private Object data;
}
