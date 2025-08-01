package com.melly.vacationmanager.domain.vacation.auditlog.repository;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationStatusChangeStatisticsResponse;

import java.util.List;

public interface VacationAuditLogRepositoryCustom {
    List<VacationStatusChangeStatisticsResponse> findMonthlyStatusChangeCounts(int year, int month);
}
