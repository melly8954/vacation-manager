package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.request.ProcessStatusRequest;
import com.melly.vacationmanager.domain.admin.dto.response.AdminUserManagePendingPageResponse;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.domain.vacation.grant.service.IVacationGrantService;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeListResponse;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.domain.vacation.type.service.IVacationTypeService;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminUserManageServiceImpl implements IAdminUserManageService {

    private final UserRepository userRepository;
    private final IVacationBalanceService vacationBalanceService;
    private final IVacationGrantService vacationGrantService;
    private final IVacationTypeService vacationTypeService;

    @Override
    public AdminUserManagePendingPageResponse findPendingUsers(Integer year, Integer month, String name, Pageable pageable) {
        // Repository 호출해서 쿼리 DSL 결과 받아옴
        Page<UserEntity> pendingUsers = userRepository.findPendingUsers(name, year, month, pageable);

        // 받아온 결과를 AdminUserManagePendingPageResponse 형태로 변환
        return AdminUserManagePendingPageResponse.from(pendingUsers);
    }

    @Override
    @Transactional
    public void processPendingUsers(Long userId, ProcessStatusRequest request) {
        // 가입 신청자 상태 변경
        if(!request.getStatus().equals("approved") && !request.getStatus().equals("rejected")) {
            throw new CustomException(ErrorCode.INVALID_STATUS);
        }
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow( () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        switch (request.getStatus()) {
            case "approved" -> {
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
                // 휴가 지급
                grantInitialVacations(user);
            }
            case "rejected" -> {
                user.setStatus(UserStatus.REJECTED);
                userRepository.save(user);
            }
        }
    }

    // 휴가 지급 로직
    private void grantInitialVacations(UserEntity user) {
        VacationTypeListResponse vacationTypes = vacationTypeService.getAllTypes();

        for (VacationTypeDto typeDto : vacationTypes.getTypes()) {
            String typeCode = typeDto.getTypeCode();

            VacationTypeEntity typeEntity = vacationTypeService.findByTypeCode(typeCode)
                    .orElseThrow(() -> new CustomException(ErrorCode.VACATION_TYPE_NOT_FOUND));

            Integer defaultDays;

            if ("ANNUAL".equals(typeCode)) {
                defaultDays = 0;  // 가입 시 연차는 0일로 지급
            } else {
                defaultDays = typeEntity.getDefaultDays() != null ? typeEntity.getDefaultDays() : 0;
            }

            boolean recordGrant = !typeCode.equals("ANNUAL");

            grantVacation(user, typeCode, defaultDays, recordGrant);
        }
    }

    private void grantVacation(UserEntity user, String typeCode, Integer days, boolean recordGrant) {
        VacationTypeEntity type = vacationTypeService.findByTypeCode(typeCode)
                .orElseThrow(() -> new CustomException(ErrorCode.VACATION_TYPE_NOT_FOUND));

        vacationBalanceService.initializeVacationBalance(user, type, days);

        if (recordGrant) {
            vacationGrantService.recordGrant(user, type, days);
        }
    }
}
