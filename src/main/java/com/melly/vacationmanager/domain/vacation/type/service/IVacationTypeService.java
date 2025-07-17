package com.melly.vacationmanager.domain.vacation.type.service;

import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;

import java.util.List;

public interface IVacationTypeService {
    List<VacationTypeDto> getAllTypes();
}
