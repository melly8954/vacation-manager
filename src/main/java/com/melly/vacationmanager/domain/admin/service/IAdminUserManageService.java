package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.response.AdminUserManagePendingPageResponse;
import org.springframework.data.domain.Pageable;

public interface IAdminUserManageService {
    AdminUserManagePendingPageResponse findPendingUsers(Integer year, Integer month, String name, Pageable pageable);
}
