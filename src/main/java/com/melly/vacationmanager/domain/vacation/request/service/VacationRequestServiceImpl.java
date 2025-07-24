package com.melly.vacationmanager.domain.vacation.request.service;

import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import com.melly.vacationmanager.domain.filestorage.repository.EvidenceFileRepository;
import com.melly.vacationmanager.domain.filestorage.service.IEvidenceFileService;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.service.IUserService;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestListResponse;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.service.IVacationTypeService;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacationRequestServiceImpl implements IVacationRequestService {
    private final VacationRequestRepository vacationRequestRepository;
    private final IUserService userService;
    private final IVacationTypeService vacationTypeService;
    private final IVacationBalanceService vacationBalanceService;
    private final EvidenceFileRepository evidenceFileRepository;
    private final IEvidenceFileService evidenceFileService;

    @Override
    public void requestVacation(VacationRequestDto requestDto, List<MultipartFile> evidenceFiles, Long userId) {
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
                    } catch (IOException e) {
                        throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
                    }
                }
            }
        }
    }

    @Override
    public Page<VacationRequestListResponse> getMyRequests(VacationRequestSearchCond cond) {
        Sort sortBy;
        if ("asc".equalsIgnoreCase(cond.getOrder())) {
            sortBy = Sort.by("createdAt").ascending();
        } else {
            sortBy = Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(cond.getPage() - 1, cond.getSize(), sortBy);

        return vacationRequestRepository.findMyVacationRequests(cond, pageable);
    }
}
