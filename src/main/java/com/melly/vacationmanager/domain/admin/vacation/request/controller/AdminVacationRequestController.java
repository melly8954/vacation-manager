package com.melly.vacationmanager.domain.admin.vacation.request.controller;

import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.VacationRequestStatusUpdateRequest;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestPageResponse;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.VacationRequestStatusUpdateResponse;
import com.melly.vacationmanager.domain.admin.vacation.request.service.IAdminVacationRequestService;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                                               @RequestParam(defaultValue = "ALL") String year,
                                                               @RequestParam(defaultValue = "ALL") String month,
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

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<ResponseDto> updateVacationRequestStatus(@PathVariable String requestId,
                                                                   @RequestBody VacationRequestStatusUpdateRequest request) {
        VacationRequestStatusUpdateResponse response = adminVacationRequestService.updateVacationRequestStatus(requestId, request);
        return makeResponseEntity(HttpStatus.OK,null,"휴가 신청내역의 상태 변경이 완료되었습니다.", response);
    }
}
