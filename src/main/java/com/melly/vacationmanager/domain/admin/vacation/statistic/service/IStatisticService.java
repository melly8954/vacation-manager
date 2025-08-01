package com.melly.vacationmanager.domain.admin.vacation.statistic.service;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;

import java.util.List;

public interface IStatisticService {

    List<VacationGrantStatisticsResponse> getVacationGrantStatistics(String year);

}
