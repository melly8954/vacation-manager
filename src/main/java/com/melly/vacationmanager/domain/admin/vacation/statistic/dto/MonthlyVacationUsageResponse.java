package com.melly.vacationmanager.domain.admin.vacation.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MonthlyVacationUsageResponse {
    private int month;
    private List<VacationUsageStatisticsResponse> vacationRequests;
}
