package com.melly.vacationmanager.batch;

import com.melly.vacationmanager.domain.vacation.grant.service.IVacationGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class VacationGrantBatchScheduler {

    private final IVacationGrantService vacationGrantService;

    @Scheduled(cron = "0 0 0 1 1 *") // 매년 1월 1일 자정
    @Transactional
    public void runAnnualGrant() {
        vacationGrantService.grantRegularVacations();
    }
}
