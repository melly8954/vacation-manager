package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.dto.request.VacationRequestStatusUpdateRequest;
import com.melly.vacationmanager.domain.admin.dto.response.AdminVacationRequestPageResponse;
import com.melly.vacationmanager.domain.admin.dto.response.VacationRequestStatusUpdateResponse;

public interface IAdminVacationRequestService {
    AdminVacationRequestPageResponse getVacationRequests(AdminVacationRequestSearchCond cond);

    VacationRequestStatusUpdateResponse updateVacationRequestStatus(String requestId, VacationRequestStatusUpdateRequest request);
}
