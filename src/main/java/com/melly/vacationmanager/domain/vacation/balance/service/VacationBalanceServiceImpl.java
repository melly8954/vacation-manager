package com.melly.vacationmanager.domain.vacation.balance.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.balance.dto.VacationBalanceListResponse;
import com.melly.vacationmanager.domain.vacation.balance.dto.VacationBalanceResponse;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.repository.VacationBalanceRepository;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacationBalanceServiceImpl implements IVacationBalanceService {

    private final VacationBalanceRepository vacationBalanceRepository;
    private final VacationGrantRepository vacationGrantRepository;
    private final VacationRequestRepository vacationRequestRepository;

    @Override
    public Optional<VacationBalanceEntity> findById(VacationBalanceId id) {
        return vacationBalanceRepository.findById(id);
    }

    @Override
    public void initializeVacationBalance(UserEntity user, VacationTypeEntity type, Integer days) {
        VacationBalanceId id = new VacationBalanceId(user.getUserId(), type.getTypeCode());

        BigDecimal bd_days = BigDecimal.valueOf(days);

        boolean exists = vacationBalanceRepository.existsById(id);
        if (!exists) {
            VacationBalanceEntity balance = VacationBalanceEntity.builder()
                    .id(id)
                    .user(user)
                    .type(type)
                    .remainingDays(bd_days)
                    .build();
            vacationBalanceRepository.save(balance);
        }
    }

    @Override
    public VacationBalanceListResponse getVacationBalancesByUserId(Long userId) {
        List<VacationBalanceEntity> entities = vacationBalanceRepository.findByUser_UserId(userId);

        List<VacationBalanceResponse> result = new ArrayList<>();

        for (VacationBalanceEntity entity : entities) {
            String typeCode = entity.getId().getTypeCode();

            BigDecimal grantedDays = vacationGrantRepository.sumGrantedDays(userId, typeCode);
            BigDecimal usedDays = vacationRequestRepository.sumUsedDays(userId, typeCode, VacationRequestStatus.APPROVED);

            result.add(VacationBalanceResponse.builder()
                    .typeCode(entity.getId().getTypeCode())
                    .typeName(entity.getType().getTypeName())
                    .grantedDays(grantedDays)
                    .usedDays(usedDays)
                    .remainingDays(entity.getRemainingDays())
                    .build());
        }

        return VacationBalanceListResponse.builder()
                .vacationBalances(result)
                .build();
    }
}
