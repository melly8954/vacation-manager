package com.melly.vacationmanager.domain.vacation.balance.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;

import java.math.BigDecimal;

public interface IVacationBalanceService {
    void initializeVacationBalance(UserEntity user, VacationTypeEntity type, BigDecimal days);
}
