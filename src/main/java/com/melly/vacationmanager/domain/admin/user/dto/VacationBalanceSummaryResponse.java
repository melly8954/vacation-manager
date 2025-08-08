package com.melly.vacationmanager.domain.admin.user.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class VacationBalanceSummaryResponse {
    private String typeCode;
    private BigDecimal remainingDays;
}