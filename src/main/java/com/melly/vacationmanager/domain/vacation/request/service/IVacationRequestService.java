package com.melly.vacationmanager.domain.vacation.request.service;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.EvidenceFileResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VRCancelResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationCalendarResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestPageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVacationRequestService {
    void requestVacation(VacationRequestDto requestDto, List<MultipartFile> evidenceFiles, Long userId);
    VacationRequestPageResponse getMyRequests(VacationRequestSearchCond cond);

    List<EvidenceFileResponse> getEvidenceFiles(Long requestId);

    VRCancelResponse cancelVacationRequest(Long requestId);

    List<VacationCalendarResponse> findApprovedVacationsForCalendar(String startDate, String endDate, Long userId);

}
