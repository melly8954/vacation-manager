package com.melly.vacationmanager.domain.admin.vacation.statistic.service;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements IStatisticService {

    private final VacationGrantRepository vacationGrantRepository;

    @Override
    public List<VacationGrantStatisticsResponse> getVacationGrantStatistics(String year) {
        LocalDate today = LocalDate.now();
        int y = parseYear(year, today);

        LocalDate start = LocalDate.of(y, 1, 1);
        LocalDate end = LocalDate.of(y, 12, 31);

        return vacationGrantRepository.findGrantStatisticsBetween(start, end);
    }

    private int parseYear(String year, LocalDate today) {
        if (year == null || year.isBlank()) {
            return today.getYear();
        }
        try {
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Year must be a valid number");
        }
    }

    private int parseMonth(String month, LocalDate today) {
        if (month == null || month.isBlank()) {
            return today.getMonthValue();
        }
        try {
            int m = Integer.parseInt(month);
            if (m < 1 || m > 12) throw new IllegalArgumentException("Month must be between 1 and 12");
            return m;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Month must be a valid number");
        }
    }
}
