package com.melly.vacationmanager.domain.admin.vacation.statistic.controller;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.MonthlyVacationUsageResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationStatusChangeStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationUsageStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.service.IStatisticService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/vacation-statistics")
public class VacationStatisticsController implements ResponseController {

    private final IStatisticService statisticService;

    @GetMapping("/grants")
    public ResponseEntity<ResponseDto> getVacationGrantStatistics(@RequestParam String year) {
        List<VacationGrantStatisticsResponse> response = statisticService.getVacationGrantStatistics(year);

        return makeResponseEntity(HttpStatus.OK, null, "휴가 지급 통계 조회 성공", response);
    }

    @GetMapping("/usages")
    public ResponseEntity<ResponseDto> getVacationUsageStatistics(@RequestParam String year) {
        List<MonthlyVacationUsageResponse> response = statisticService.getUsageStatistics(year);

        return makeResponseEntity(HttpStatus.OK, null, "휴가 지급 통계 조회 성공", response);
    }

    @GetMapping("/status-changes")
    public ResponseEntity<ResponseDto> getVacationStatusStatistics(@RequestParam String year,
                                                                   @RequestParam String month) {
        List<VacationStatusChangeStatisticsResponse> response = statisticService.getStatusChangeStatistics(year,month);

        return makeResponseEntity(HttpStatus.OK, null, "휴가 지급 통계 조회 성공", response);
    }
}
