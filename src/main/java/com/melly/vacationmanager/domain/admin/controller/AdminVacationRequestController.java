package com.melly.vacationmanager.domain.admin.controller;

import com.melly.vacationmanager.domain.admin.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.dto.response.AdminVacationRequestPageResponse;
import com.melly.vacationmanager.domain.admin.service.IAdminVacationRequestService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/vacation-requests")
public class AdminVacationRequestController implements ResponseController {

    private final IAdminVacationRequestService adminVacationRequestService;

    @GetMapping("")
    public ResponseEntity<ResponseDto> findAllVacationRequests(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(required = false) String name,
                                                               @RequestParam(defaultValue = "ALL") String typeCode,
                                                               @RequestParam(defaultValue = "ALL") String status,
                                                               @RequestParam(required = false) String year,
                                                               @RequestParam(required = false) String month,
                                                               @RequestParam(defaultValue = "desc") String order) {
        AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                .page(page)
                .size(size)
                .name(name)
                .typeCode(typeCode)
                .status(status)
                .year(year)
                .month(month)
                .order(order)
                .build();
        AdminVacationRequestPageResponse response = adminVacationRequestService.getVacationRequests(cond);
        return makeResponseEntity(HttpStatus.OK,null,"내 휴가 신청 내역을 성공적으로 조회했습니다.", response);
    }

}
