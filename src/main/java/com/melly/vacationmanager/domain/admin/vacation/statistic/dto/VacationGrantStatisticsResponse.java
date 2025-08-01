package com.melly.vacationmanager.domain.admin.vacation.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class VacationGrantStatisticsResponse {
    private String typeCode;         // 휴가 유형 코드
    private String typeName;         // 휴가 유형 이름
    private BigDecimal totalGrantedDays;  // 지급된 총 일수
}
