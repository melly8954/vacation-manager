package com.melly.vacationmanager.domain.vacation.request.repository;

import java.time.LocalDate;

public interface VacationRequestRepositoryCustom {
    boolean existsApprovedOverlap(Long userId, LocalDate startDate, LocalDate endDate);
}
