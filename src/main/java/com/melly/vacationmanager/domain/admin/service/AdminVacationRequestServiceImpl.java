package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.dto.request.VacationRequestStatusUpdateRequest;
import com.melly.vacationmanager.domain.admin.dto.response.AdminVacationRequestListResponse;
import com.melly.vacationmanager.domain.admin.dto.response.AdminVacationRequestPageResponse;
import com.melly.vacationmanager.domain.admin.dto.response.VacationRequestStatusUpdateResponse;
import com.melly.vacationmanager.domain.vacation.auditlog.entity.VacationAuditLogEntity;
import com.melly.vacationmanager.domain.vacation.auditlog.repository.VacationAuditLogRepository;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestPageResponse;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import com.melly.vacationmanager.global.common.utils.CurrentUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminVacationRequestServiceImpl implements IAdminVacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final VacationAuditLogRepository vacationAuditLogRepository;

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

    @Override
    @Transactional
    public VacationRequestStatusUpdateResponse updateVacationRequestStatus(String requestId, VacationRequestStatusUpdateRequest request) {
        VacationRequestEntity findEntity = vacationRequestRepository.findById(Long.parseLong(requestId))
                .orElseThrow(() -> new CustomException(ErrorCode.VACATION_REQUEST_NOT_FOUND));

        VacationRequestStatus newStatus = VacationRequestStatus.valueOf(request.getStatus());
        VacationRequestStatus oldStatus = findEntity.getStatus();

        findEntity.updateStatus(newStatus);

        vacationAuditLogRepository.save(VacationAuditLogEntity.builder()
                .request(findEntity)
                .changedBy(CurrentUserUtils.getUser())
                .changedByRole(CurrentUserUtils.getRole())
                .changeDate(LocalDateTime.now())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .comment("관리자의 휴가 신청내역 처리")
                .build());

        return new VacationRequestStatusUpdateResponse(
                findEntity.getRequestId(),
                newStatus.name()
        );
    }
}
