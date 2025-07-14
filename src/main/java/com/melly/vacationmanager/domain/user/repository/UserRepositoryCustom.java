package com.melly.vacationmanager.domain.user.repository;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<UserEntity> findPendingUsers(String name, Integer year, Integer month, Pageable pageable);
}
