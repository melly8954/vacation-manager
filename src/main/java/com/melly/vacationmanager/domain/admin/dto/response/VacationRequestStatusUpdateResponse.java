package com.melly.vacationmanager.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VacationRequestStatusUpdateResponse {
    private Long requestId;
    private String newStatus;
}
