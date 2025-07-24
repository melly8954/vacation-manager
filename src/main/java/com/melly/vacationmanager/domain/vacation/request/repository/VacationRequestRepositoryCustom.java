package com.melly.vacationmanager.domain.vacation.request.repository;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface VacationRequestRepositoryCustom {
    boolean existsApprovedOverlap(Long userId, LocalDate startDate, LocalDate endDate);
    Page<VacationRequestListResponse> findMyVacationRequests(VacationRequestSearchCond cond, Pageable pageable);
}
