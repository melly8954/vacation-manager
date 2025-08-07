package com.melly.vacationmanager.domain.vacation.type.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VacationTypeDto {
    private String typeCode;
    private String typeName;
}
