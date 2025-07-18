package com.melly.vacationmanager.domain.vacation.request.controller;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
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
}
