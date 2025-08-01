package com.melly.vacationmanager.domain.vacation.grant.repository;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationGrantRepositoryCustom {
    List<VacationGrantStatisticsResponse> findGrantStatisticsBetween(LocalDate start, LocalDate end);
}
