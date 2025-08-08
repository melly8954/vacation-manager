package com.melly.vacationmanager.domain.vacation.request.service;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVacationRequestService {
    VacationRequestCreateResponse requestVacation(VacationRequestDto requestDto, List<MultipartFile> evidenceFiles, Long userId);
    VacationRequestPageResponse getMyRequests(VacationRequestSearchCond cond);

    EvidenceFileListResponse getEvidenceFiles(Long requestId);

    VRCancelResponse cancelVacationRequest(Long requestId);

    VacationCalendarListResponse findApprovedVacationsForCalendar(String startDate, String endDate, Long userId);

}
