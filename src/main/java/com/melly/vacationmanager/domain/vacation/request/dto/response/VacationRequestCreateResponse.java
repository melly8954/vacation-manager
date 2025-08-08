package com.melly.vacationmanager.domain.vacation.request.dto.response;

import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class VacationRequestCreateResponse {
    private String typeCode;
    private LocalDate  startDate;
    private LocalDate endDate;
    private BigDecimal daysCount;
    private String reason;
    private VacationRequestStatus status;
    private LocalDateTime createdAt;

    private List<EvidenceFileResponse> evidenceFiles;

    public static VacationRequestCreateResponse from(VacationRequestEntity entity, List<EvidenceFileResponse> files) {
        return VacationRequestCreateResponse.builder()
                .typeCode(entity.getVacationType().getTypeCode())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .daysCount(entity.getDaysCount())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .evidenceFiles(files)
                .build();
    }
}
