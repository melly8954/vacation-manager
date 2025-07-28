package com.melly.vacationmanager.domain.admin.vacation.request.service;

import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.VacationRequestStatusUpdateRequest;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestListResponse;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestPageResponse;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.VacationRequestStatusUpdateResponse;
import com.melly.vacationmanager.domain.vacation.auditlog.entity.VacationAuditLogEntity;
import com.melly.vacationmanager.domain.vacation.auditlog.repository.VacationAuditLogRepository;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.repository.VacationBalanceRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminVacationRequestServiceImpl implements IAdminVacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final VacationAuditLogRepository vacationAuditLogRepository;
    private final VacationBalanceRepository vacationBalanceRepository;

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

        if (newStatus == VacationRequestStatus.APPROVED) {
            boolean hasOverlap = vacationRequestRepository.existsApprovedOverlap(
                    findEntity.getUser().getUserId(),
                    findEntity.getStartDate(),
                    findEntity.getEndDate()
            );

            if (hasOverlap) {
                throw new CustomException(ErrorCode.OVERLAPPING_APPROVED_VACATION);
            }

            deductVacationDays(findEntity);
        }

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

    private void deductVacationDays(VacationRequestEntity request) {
        VacationBalanceId balanceId = new VacationBalanceId(request.getUser().getUserId(), request.getVacationType().getTypeCode());
        VacationBalanceEntity balance = vacationBalanceRepository.findById(balanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.VACATION_BALANCE_NOT_FOUND));

        BigDecimal remainingDays = balance.getRemainingDays();
        BigDecimal daysToDeduct = request.getDaysCount();

        if (remainingDays.compareTo(daysToDeduct) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        balance.setRemainingDays(remainingDays.subtract(daysToDeduct));
        vacationBalanceRepository.save(balance);
    }
}
