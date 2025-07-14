package com.melly.vacationmanager.domain.user.repository;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.global.common.enums.UserRole;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryCustom  {
    int countByRole(UserRole role);

    Optional<UserEntity> findByUsername(String value);
    Optional<UserEntity> findByEmail(String value);

    Optional<UserEntity> findByUserId(Long userId);
}
