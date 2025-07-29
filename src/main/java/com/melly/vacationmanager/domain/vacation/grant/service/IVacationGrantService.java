package com.melly.vacationmanager.domain.vacation.grant.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;

public interface IVacationGrantService {
    void recordGrant(UserEntity user, VacationTypeEntity type, Integer days);
    void grantRegularVacations();
}
