package com.melly.vacationmanager.domain.vacation.request.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VacationRequestDto {
    @NotBlank(message = "휴가 유형은 필수 입력 항목입니다.")
    private String typeCode;

    @NotNull(message = "휴가 시작일은 필수 입력 항목입니다.")
    private LocalDate startDate;

    @NotNull(message = "휴가 종료일은 필수 입력 항목입니다.")
    private LocalDate endDate;

    @NotNull(message = "휴가 일수는 필수 입력 항목입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "휴가 일수는 0일 이상이어야 합니다.")
    private BigDecimal daysCount;

    @NotBlank(message = "휴가 사유는 필수 입력 항목입니다.")
    private String reason;
}
