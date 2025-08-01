package com.melly.vacationmanager.domain.admin.vacation.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VacationStatusChangeStatisticsResponse {
    private String newStatus;
    private Long totalCount;
}
