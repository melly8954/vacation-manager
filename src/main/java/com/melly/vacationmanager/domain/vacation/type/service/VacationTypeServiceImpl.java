package com.melly.vacationmanager.domain.vacation.type.service;

import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacationTypeServiceImpl implements IVacationTypeService {
    private final VacationTypeRepository vacationTypeRepository;

    @Override
    public List<VacationTypeDto> getAllTypes() {
        List<VacationTypeEntity> entities = vacationTypeRepository.findAll();
        return entities.stream()
                .map(e -> new VacationTypeDto(e.getTypeCode(), e.getTypeName()))
                .collect(Collectors.toList());
    }
}
