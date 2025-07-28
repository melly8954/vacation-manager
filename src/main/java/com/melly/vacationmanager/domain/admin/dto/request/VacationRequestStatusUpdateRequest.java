package com.melly.vacationmanager.domain.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VacationRequestStatusUpdateRequest {
    private String status;
}
