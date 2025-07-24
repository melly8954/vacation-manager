package com.melly.vacationmanager.domain.vacation.request.service;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestListResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVacationRequestService {
    void requestVacation(VacationRequestDto requestDto, List<MultipartFile> evidenceFiles, Long userId);
    Page<VacationRequestListResponse> getMyRequests(VacationRequestSearchCond cond);
}
