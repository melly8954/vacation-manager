package com.melly.vacationmanager.domain.vacation.request.repository;

import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestListResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationStatusChangeStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationUsageStatisticsRaw;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationUsageStatisticsResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationCalendarResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface VacationRequestRepositoryCustom {
    boolean existsApprovedOverlap(Long userId, LocalDate startDate, LocalDate endDate);
    Page<VacationRequestListResponse> findMyVacationRequests(VacationRequestSearchCond cond, Pageable pageable);
    Page<AdminVacationRequestListResponse> findAllVacationRequestsForAdmin(AdminVacationRequestSearchCond cond, Pageable pageable);

    List<VacationCalendarResponse> findApprovedVacationsForCalendar(Long userId, LocalDate start, LocalDate end);

    List<VacationUsageStatisticsRaw> findUsageStatisticsByYear(int year);
    List<VacationStatusChangeStatisticsResponse> findMonthlyStatusChangeCounts(int year, int month);

}
