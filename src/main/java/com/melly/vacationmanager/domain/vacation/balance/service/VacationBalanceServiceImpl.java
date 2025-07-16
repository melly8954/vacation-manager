package com.melly.vacationmanager.domain.vacation.balance.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.repository.VacationBalanceRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VacationBalanceServiceImpl implements IVacationBalanceService {

    private final VacationBalanceRepository vacationBalanceRepository;

    @Override
    public void initializeVacationBalance(UserEntity user, VacationTypeEntity type, BigDecimal days) {
        VacationBalanceId id = new VacationBalanceId(user.getUserId(), type.getTypeCode());

        VacationBalanceEntity balance = vacationBalanceRepository.findById(id)
                .orElseGet(() -> VacationBalanceEntity.builder()
                        .id(id)
                        .user(user)
                        .type(type)
                        .remainingDays(days)
                        .build());

        vacationBalanceRepository.save(balance);
    }
}
