package com.melly.vacationmanager.domain.admin.dto.response;

import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminVacationRequestListResponse {

    private Long requestId;
    private Long userId;
    private String name;
    private String username;
    private String typeCode;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal daysCount;
    private String reason;
    private LocalDateTime createdAt;
}
