package com.melly.vacationmanager.domain.vacation.request.controller;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.EvidenceFileResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VRCancelResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestPageResponse;
import com.melly.vacationmanager.domain.vacation.request.service.IVacationRequestService;
import com.melly.vacationmanager.global.auth.PrincipalDetails;
import com.melly.vacationmanager.global.common.controller.ResponseController;
import com.melly.vacationmanager.global.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vacation-requests")
public class VacationRequestController implements ResponseController {

    private final IVacationRequestService vacationRequestService;

    @PostMapping("")
    public ResponseEntity<ResponseDto> requestVacation(@RequestPart(value = "request_data") @Validated VacationRequestDto request,
                                                       @RequestPart(value = "evidence_files", required = false) List<MultipartFile> evidenceFiles,
                                                       @AuthenticationPrincipal PrincipalDetails userDetails) {
        Long userId = userDetails.getUserEntity().getUserId();
        vacationRequestService.requestVacation(request, evidenceFiles, userId);
        return makeResponseEntity(HttpStatus.OK,null,"휴가 신청이 완료되었습니다.",null);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto> getMyVacationRequests( @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(name = "type-code", defaultValue = "ALL") String typeCode,
                                                              @RequestParam(defaultValue = "ALL") String status,
                                                              @RequestParam(defaultValue = "ALL") String year,
                                                              @RequestParam(defaultValue = "ALL") String month,
                                                              @RequestParam(defaultValue = "desc") String order,
                                                              @RequestParam(defaultValue = "createdAt") String dateFilterType,
                                                              @AuthenticationPrincipal PrincipalDetails principal) {
        Long userId = principal.getUserEntity().getUserId(); // 인증 사용자 ID

        VacationRequestSearchCond cond = new VacationRequestSearchCond(
                userId, typeCode, status, year, month, order, dateFilterType, page, size
        );

        VacationRequestPageResponse result = vacationRequestService.getMyRequests(cond);

        return makeResponseEntity(HttpStatus.OK,null,"내 휴가 신청 내역을 성공적으로 조회했습니다.", result);
    }

    @GetMapping("/{requestId}/evidence-files")
    public ResponseEntity<ResponseDto> getEvidenceFiles(@PathVariable Long requestId) {
        List<EvidenceFileResponse> evidenceFiles = vacationRequestService.getEvidenceFiles(requestId);
        String message = evidenceFiles.isEmpty() ? "증빙자료가 존재하지 않습니다." : "성공적으로 증빙자료를 조회했습니다.";
        return makeResponseEntity(HttpStatus.OK, null, message, evidenceFiles);
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<ResponseDto> cancelVacationRequest(@PathVariable Long requestId) {
        VRCancelResponse response = vacationRequestService.cancelVacationRequest(requestId);
        return makeResponseEntity(HttpStatus.OK,null,"휴가 신청이 취소되었습니다.", response);
    }
}
