package com.melly.vacationmanager.domain.user.service;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용자 가입
    public void signUp(SignUpRequest request) {
        if(!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            log.error("비밀번호와 비밀번호 확인이 일치하지 않습니다");
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (request.getHiredate().isAfter(LocalDate.now())) {
            log.error("입사일은 오늘 이후 날짜일 수 없습니다.");
            throw new CustomException(ErrorCode.INVALID_FORMAT_HIREDATE);
        }

        UserEntity userEntity = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .hireDate(request.getHiredate())
                .position(request.getPosition())
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();
        userRepository.save(userEntity);
    }

    // 아이디 중복체크
}
