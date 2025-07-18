package com.melly.vacationmanager.domain.vacation.type.service;

import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeListResponse;

import java.util.List;

public interface IVacationTypeService {
    VacationTypeListResponse getAllTypes();
}
