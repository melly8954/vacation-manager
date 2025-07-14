package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.response.AdminUserManagePendingPageResponse;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserManageServiceImpl implements IAdminUserManageService {

    private final UserRepository userRepository;

    @Override
    public AdminUserManagePendingPageResponse findPendingUsers(int year, int month, String name, Pageable pageable) {
        // Repository 호출해서 쿼리 DSL 결과 받아옴
        Page<UserEntity> pendingUsers = userRepository.findPendingUsers(name, year, month, pageable);

        // 받아온 결과를 AdminUserManagePendingPageResponse 형태로 변환
        return AdminUserManagePendingPageResponse.from(pendingUsers);
    }
}
