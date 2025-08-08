package com.melly.vacationmanager.domain.admin.vacation.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class VacationUsageStatisticsRaw {
    private String typeCode;
    private String typeName;
    private int month;
    private BigDecimal totalUsedDays;
}
