package com.melly.vacationmanager.domain.vacation.type.service;

import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeListResponse;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacationTypeServiceImpl implements IVacationTypeService {
    private final VacationTypeRepository vacationTypeRepository;

    @Override
    public VacationTypeListResponse getAllTypes() {
        List<VacationTypeEntity> entities = vacationTypeRepository.findAll();
        List<VacationTypeDto> dtoList = entities.stream()
                .map(e -> new VacationTypeDto(e.getTypeCode(), e.getTypeName()))
                .collect(Collectors.toList());
        return new VacationTypeListResponse(dtoList);
    }

    @Override
    public Optional<VacationTypeEntity> findByTypeCode(String typeCode) {
        return vacationTypeRepository.findByTypeCode(typeCode);
    }
}
