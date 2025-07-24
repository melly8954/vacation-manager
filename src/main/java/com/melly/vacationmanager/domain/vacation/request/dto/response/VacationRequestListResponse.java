package com.melly.vacationmanager.domain.vacation.request.dto.response;

import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class VacationRequestListResponse {
    private Long requestId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String typeCode;
    private BigDecimal daysCount;
    private VacationRequestStatus status;
    private String reason;
    private LocalDateTime createdAt;
}
