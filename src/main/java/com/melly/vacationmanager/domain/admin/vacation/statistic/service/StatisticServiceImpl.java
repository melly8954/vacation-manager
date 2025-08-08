package com.melly.vacationmanager.domain.admin.vacation.statistic.service;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.*;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.global.common.utils.DateParseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        // 1. DB 쿼리 결과(월별, 타입별 통계 Raw 데이터)
        List<VacationUsageStatisticsRaw> flatList = vacationRequestRepository.findUsageStatisticsByYear(y);

        // 2. 월별로 그룹핑 (key = month, value = 해당 월 데이터 리스트)
        Map<Integer, List<VacationUsageStatisticsRaw>> groupedByMonth = flatList.stream()
                .collect(Collectors.groupingBy(VacationUsageStatisticsRaw::getMonth));

        // 3. MonthlyVacationUsageResponse DTO로 변환
        return groupedByMonth.entrySet().stream()
                .map(entry -> {
                    int month = entry.getKey();

                    // Raw -> 리턴용 DTO 변환: VacationUsageStatisticsRaw -> VacationUsageStatisticsResponse (month 제외)
                    List<VacationUsageStatisticsResponse> vacations = entry.getValue().stream()
                            .map(raw -> new VacationUsageStatisticsResponse(
                                    raw.getTypeCode(),
                                    raw.getTypeName(),
                                    raw.getTotalUsedDays()
                            ))
                            .collect(Collectors.toList());

                    return new MonthlyVacationUsageResponse(month, vacations);
                })
                // 월별 오름차순 정렬
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
