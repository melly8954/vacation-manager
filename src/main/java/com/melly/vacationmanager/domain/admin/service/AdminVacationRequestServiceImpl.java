package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.dto.response.AdminVacationRequestListResponse;
import com.melly.vacationmanager.domain.admin.dto.response.AdminVacationRequestPageResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestPageResponse;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminVacationRequestServiceImpl implements IAdminVacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;

    @Override
    public AdminVacationRequestPageResponse getVacationRequests(AdminVacationRequestSearchCond cond) {
        Sort sortBy;
        if ("asc".equalsIgnoreCase(cond.getOrder())) {
            sortBy = Sort.by("createdAt").ascending();
        } else {
            sortBy = Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(cond.getPage() - 1, cond.getSize(), sortBy);
        Page<AdminVacationRequestListResponse> page = vacationRequestRepository.findAllVacationRequestsForAdmin(cond, pageable);
        return AdminVacationRequestPageResponse.fromPage(page);
    }
}
