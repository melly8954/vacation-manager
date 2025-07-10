package com.melly.vacationmanager.global.config;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}") private String adminUsername;
    @Value("${admin.password}") private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(UserRole.ADMIN) == 0) {
            UserEntity admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("ADMIN")
                    .status(UserStatus.ACTIVE)
                    .role(UserRole.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            log.info("관리자 계정이 생성되었습니다.");
        }else{
            log.info("이미 최상위 관리자 계정은 존재하는 중 입니다.");
        }
    }
}
