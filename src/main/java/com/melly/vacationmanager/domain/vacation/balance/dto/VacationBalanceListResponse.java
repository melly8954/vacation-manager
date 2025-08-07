package com.melly.vacationmanager.domain.vacation.balance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class VacationBalanceListResponse {
    private List<VacationBalanceResponse> vacationBalances;
}
