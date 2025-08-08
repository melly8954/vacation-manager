package com.melly.vacationmanager.domain.admin.vacation.statistic.service;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.MonthlyVacationUsageResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationStatusChangeStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationUsageStatisticsResponse;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.global.common.utils.DateParseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements IStatisticService {

    private final VacationGrantRepository vacationGrantRepository;
    private final VacationRequestRepository vacationRequestRepository;

    @Override
    public List<VacationGrantStatisticsResponse> getVacationGrantStatistics(String year) {
        LocalDate today = LocalDate.now();
        int y = DateParseUtils.parseYear(year, today);

        LocalDate start = LocalDate.of(y, 1, 1);
        LocalDate end = LocalDate.of(y, 12, 31);

        return vacationGrantRepository.findGrantStatisticsBetween(start, end);
    }

    @Override
    public List<MonthlyVacationUsageResponse> getUsageStatistics(String year) {
        int y = DateParseUtils.parseYear(year, LocalDate.now());
        List<VacationUsageStatisticsResponse> flatList = vacationRequestRepository.findUsageStatisticsByYear(y);

        return flatList.stream()
                .collect(Collectors.groupingBy(VacationUsageStatisticsResponse::getMonth))
                .entrySet().stream()
                .map(entry -> new MonthlyVacationUsageResponse(
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(MonthlyVacationUsageResponse::getMonth))
                .toList();
    }

    @Override
    public List<VacationStatusChangeStatisticsResponse> getStatusChangeStatistics(String year, String month) {
        int y = DateParseUtils.parseYear(year, LocalDate.now());
        int m = DateParseUtils.parseMonth(month, LocalDate.now());

        return vacationRequestRepository.findMonthlyStatusChangeCounts(y, m);
    }
}
