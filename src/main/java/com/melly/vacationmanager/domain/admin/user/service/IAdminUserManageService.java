package com.melly.vacationmanager.domain.admin.user.service;

import com.melly.vacationmanager.domain.admin.user.dto.ProcessStatusRequest;
import com.melly.vacationmanager.domain.admin.user.dto.AdminUserManagePendingPageResponse;
import com.melly.vacationmanager.domain.admin.user.dto.UserStatusChangeResponse;
import org.springframework.data.domain.Pageable;

public interface IAdminUserManageService {
    AdminUserManagePendingPageResponse findPendingUsers(String year, String month, String name, Pageable pageable);

    UserStatusChangeResponse processPendingUsers(Long userId, ProcessStatusRequest request);
}
