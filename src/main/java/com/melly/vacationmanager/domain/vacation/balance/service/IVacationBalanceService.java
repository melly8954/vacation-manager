package com.melly.vacationmanager.domain.vacation.balance.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;

import java.util.Optional;

public interface IVacationBalanceService {
    Optional<VacationBalanceEntity> findById(VacationBalanceId id);
    void initializeVacationBalance(UserEntity user, VacationTypeEntity type, Integer days);
}
