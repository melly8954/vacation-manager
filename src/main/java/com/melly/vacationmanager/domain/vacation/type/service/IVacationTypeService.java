package com.melly.vacationmanager.domain.vacation.type.service;

import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeListResponse;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;

import java.util.List;
import java.util.Optional;

public interface IVacationTypeService {
    VacationTypeListResponse getAllTypes();
    Optional<VacationTypeEntity> findByTypeCode(String typeCode);
}
