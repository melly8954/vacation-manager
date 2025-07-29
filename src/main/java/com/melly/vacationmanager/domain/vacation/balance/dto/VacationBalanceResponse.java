package com.melly.vacationmanager.domain.vacation.balance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class VacationBalanceResponse {
    private String typeCode;     // ANNUAL, SICK 등
    private String typeName;     // 연차, 병가 등
    private BigDecimal grantedDays;  // 부여일수
    private BigDecimal usedDays;     // 사용일수
    private BigDecimal remainingDays;// 잔여일수
}
