package com.melly.vacationmanager.domain.vacation.request.service;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IVacationRequestService {
    void requestVacation(VacationRequestDto requestDto, List<MultipartFile> evidenceFiles, Long userId);
}
