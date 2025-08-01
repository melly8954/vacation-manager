package com.melly.vacationmanager.domain.admin.vacation.statistic.service;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationStatusChangeStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationUsageStatisticsResponse;

import java.util.List;

public interface IStatisticService {

    List<VacationGrantStatisticsResponse> getVacationGrantStatistics(String year);

    List<VacationUsageStatisticsResponse> getUsageStatistics(String year);

    List<VacationStatusChangeStatisticsResponse> getStatusChangeStatistics(String year, String month);

}
