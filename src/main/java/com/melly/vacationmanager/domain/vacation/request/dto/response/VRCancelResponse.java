package com.melly.vacationmanager.domain.vacation.request.dto.response;

import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VRCancelResponse {
    private Long requestId;
    private String newStatus;

    public static VRCancelResponse fromEntity(Long requestId, VacationRequestStatus status) {
        return VRCancelResponse.builder()
                .requestId(requestId)
                .newStatus(status.name())
                .build();
    }
}
