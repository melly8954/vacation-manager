package com.melly.vacationmanager.domain.admin.vacation.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class VacationUsageStatisticsResponse {
    private String typeCode;
    private String typeName;
    private BigDecimal totalUsedDays;
}
