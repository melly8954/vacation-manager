package com.melly.vacationmanager.domain.vacation.type.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class VacationTypeListResponse {
    @JsonProperty("types")
    private List<VacationTypeDto> types;
}