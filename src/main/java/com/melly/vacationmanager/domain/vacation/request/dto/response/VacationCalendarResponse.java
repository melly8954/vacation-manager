package com.melly.vacationmanager.domain.vacation.request.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VacationCalendarResponse {
    private Long requestId;
    private String typeCode;
    private String typeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysCount;
    private String status;
}
