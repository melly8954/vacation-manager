package com.melly.vacationmanager.domain.vacation.request.dto.response;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VacationCalendarListResponse {
    private List<VacationCalendarResponse> vacationEvents;
}
