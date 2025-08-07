package com.melly.vacationmanager.domain.vacation.request.service;

import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import com.melly.vacationmanager.domain.filestorage.repository.EvidenceFileRepository;
import com.melly.vacationmanager.domain.filestorage.service.IEvidenceFileService;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.service.IUserService;
import com.melly.vacationmanager.domain.vacation.auditlog.entity.VacationAuditLogEntity;
import com.melly.vacationmanager.domain.vacation.auditlog.repository.VacationAuditLogRepository;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.*;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.service.IVacationTypeService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacationRequestServiceImpl implements IVacationRequestService {
    private final VacationRequestRepository vacationRequestRepository;
    private final IUserService userService;
    private final IVacationTypeService vacationTypeService;
    private final IVacationBalanceService vacationBalanceService;
    private final EvidenceFileRepository evidenceFileRepository;
    private final IEvidenceFileService evidenceFileService;
    private final VacationAuditLogRepository vacationAuditLogRepository;

    @Override
    public VacationRequestCreateResponse requestVacation(VacationRequestDto requestDto, List<MultipartFile> evidenceFiles, Long userId) {
        String typeCode = requestDto.getTypeCode();
        LocalDate startDate = requestDto.getStartDate();
        LocalDate endDate = requestDto.getEndDate();
        BigDecimal daysCount = requestDto.getDaysCount();

        UserEntity user = userService.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        VacationTypeEntity vtEntity = vacationTypeService.findByTypeCode(typeCode)
                .orElseThrow(() -> new CustomException(ErrorCode.VACATION_TYPE_NOT_FOUND));

        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (daysCount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorCode.INVALID_DAYS_COUNT);
        }

        VacationBalanceId id = new VacationBalanceId(userId, typeCode);
        VacationBalanceEntity balance = vacationBalanceService.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VACATION_BALANCE_NOT_FOUND));

        if (balance.getRemainingDays().compareTo(daysCount) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        boolean hasOverlap = vacationRequestRepository.existsApprovedOverlap(userId, startDate, endDate);
        if (hasOverlap) {
            throw new CustomException(ErrorCode.OVERLAPPING_APPROVED_VACATION);
        }

        VacationRequestEntity vrEntity = VacationRequestEntity.builder()
                .user(user)
                .vacationType(vtEntity)
                .startDate(startDate)
                .endDate(endDate)
                .daysCount(daysCount)
                .reason(requestDto.getReason())
                .status(VacationRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        vacationRequestRepository.save(vrEntity);

        // 증빙 자료 저장
        List<EvidenceFileEntity> savedEvidenceFiles = new ArrayList<>();

        if (evidenceFiles != null && !evidenceFiles.isEmpty()) {
            int fileOrder = 0;
            for (MultipartFile file : evidenceFiles) {
                if (!file.isEmpty()) {
                    try {
                        String uniqueName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        String fileUrl = evidenceFileService.saveEvidenceFile(uniqueName, file.getBytes());

                        EvidenceFileEntity evidence = EvidenceFileEntity.builder()
                                .vacationRequest(vrEntity)
                                .isUsed(false)
                                .originalName(file.getOriginalFilename())
                                .uniqueName(uniqueName)
                                .savedPath(fileUrl) // 접근 가능한 URL
                                .fileSize(file.getSize())
                                .fileType(file.getContentType())
                                .fileOrder(fileOrder++)
                                .build();
                        evidenceFileRepository.save(evidence);
                        savedEvidenceFiles.add(evidence);
                    } catch (IOException e) {
                        throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
                    }
                }
            }
        }

        List<EvidenceFileResponse> evidenceFileResponses = savedEvidenceFiles.isEmpty()
                ? Collections.emptyList()
                : savedEvidenceFiles.stream()
                .map(EvidenceFileResponse::from)
                .collect(Collectors.toList());

        return VacationRequestCreateResponse.from(vrEntity, evidenceFileResponses);
    }

    @Override
    public VacationRequestPageResponse getMyRequests(VacationRequestSearchCond cond) {
        Sort sortBy;
        if ("asc".equalsIgnoreCase(cond.getOrder())) {
            sortBy = Sort.by("createdAt").ascending();
        } else {
            sortBy = Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(cond.getPage() - 1, cond.getSize(), sortBy);
        Page<VacationRequestListResponse> page = vacationRequestRepository.findMyVacationRequests(cond, pageable);
        return VacationRequestPageResponse.from(page);
    }

    @Override
    public List<EvidenceFileResponse> getEvidenceFiles(Long requestId) {
        if(!vacationRequestRepository.existsByRequestId(requestId)){
            throw new CustomException(ErrorCode.VACATION_REQUEST_NOT_FOUND);
        }

        List<EvidenceFileEntity> files = evidenceFileRepository.findAllByVacationRequest_RequestId(requestId);
        if (files.isEmpty()) {
            // 필요하다면 빈 리스트 반환하거나 별도 처리
            return Collections.emptyList();
        }

        return files.stream()
                .map(EvidenceFileResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VRCancelResponse cancelVacationRequest(Long requestId) {
        VacationRequestEntity findEntity = vacationRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.VACATION_REQUEST_NOT_FOUND));

        VacationRequestStatus oldStatus = findEntity.getStatus();

        findEntity.cancel();    // Dirty Checking

        VacationAuditLogEntity log = VacationAuditLogEntity.builder()
                .request(findEntity)
                .changedBy(CurrentUserUtils.getUser()) // 로그인 사용자 ID
                .changedByRole(CurrentUserUtils.getRole()) // 예: ADMIN, USER
                .oldStatus(oldStatus.name())
                .newStatus(findEntity.getStatus().name())
                .comment("사용자 직접 취소") // 필요 시 외부 입력도 가능
                .build();

        vacationAuditLogRepository.save(log); // 함께 저장

        return VRCancelResponse.fromEntity(findEntity.getRequestId(), findEntity.getStatus());
    }

    @Override
    public List<VacationCalendarResponse> findApprovedVacationsForCalendar(String startDateStr, String endDateStr, Long userId) {
        LocalDate start = LocalDate.parse(startDateStr);  // "2025-06-29" 같은 날짜 형식으로 들어옴
        LocalDate end = LocalDate.parse(endDateStr);      // "2025-08-02"

        return vacationRequestRepository.findApprovedVacationsForCalendar(userId, start, end);
    }
}
