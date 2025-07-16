package com.melly.vacationmanager.domain.vacation.grant.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.grant.entity.VacationGrantEntity;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VacationGrantServiceImpl implements IVacationGrantService{

    private final VacationGrantRepository vacationGrantRepository;

    @Override
    public void recordGrant(UserEntity user, VacationTypeEntity type, Integer days) {
        VacationGrantEntity grant = VacationGrantEntity.builder()
                .user(user)
                .type(type)
                .grantDate(LocalDate.now())
                .grantedDays(days)
                .build();
        vacationGrantRepository.save(grant);
    }
}
