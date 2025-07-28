package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.dto.response.AdminVacationRequestPageResponse;

public interface IAdminVacationRequestService {
    AdminVacationRequestPageResponse getVacationRequests(AdminVacationRequestSearchCond cond);
}
