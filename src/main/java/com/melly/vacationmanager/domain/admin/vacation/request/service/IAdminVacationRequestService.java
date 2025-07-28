package com.melly.vacationmanager.domain.admin.vacation.request.service;

import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.VacationRequestStatusUpdateRequest;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestPageResponse;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.VacationRequestStatusUpdateResponse;

public interface IAdminVacationRequestService {
    AdminVacationRequestPageResponse getVacationRequests(AdminVacationRequestSearchCond cond);

    VacationRequestStatusUpdateResponse updateVacationRequestStatus(String requestId, VacationRequestStatusUpdateRequest request);
}
