package com.melly.vacationmanager.domain.admin.user.dto;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class UserStatusChangeResponse {
    private Long userId;
    private String status;
    private List<VacationBalanceSummaryResponse> vacationBalances;
}
