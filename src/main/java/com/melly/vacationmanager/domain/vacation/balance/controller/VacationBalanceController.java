package com.melly.vacationmanager.domain.vacation.balance.controller;

import com.melly.vacationmanager.domain.vacation.balance.dto.VacationBalanceResponse;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import com.melly.vacationmanager.global.common.utils.CurrentUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vacation-balances")
public class VacationBalanceController implements ResponseController {
    private final IVacationBalanceService vacationBalanceService;

    @GetMapping("/me")
    public ResponseEntity<ResponseDto> getMyVacationBalance(){
        Long userId = CurrentUserUtils.getUserId();
        List<VacationBalanceResponse> responses = vacationBalanceService.getVacationBalancesByUserId(userId);
        return makeResponseEntity(HttpStatus.OK,null,"휴가 잔여일 정보를 성공적으로 조회했습니다.",responses);
    }
}
