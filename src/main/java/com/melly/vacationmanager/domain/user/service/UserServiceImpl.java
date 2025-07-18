package com.melly.vacationmanager.domain.user.service;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;
import com.melly.vacationmanager.domain.user.dto.response.UserInfoResponse;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.UserPosition;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // 사용자 가입
    @Override
    public void signUp(SignUpRequest request) {
        if(!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            log.error("비밀번호와 비밀번호 확인이 일치하지 않습니다");
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (request.getHireDate().isAfter(LocalDate.now())) {
            log.error("입사일은 오늘 이후 날짜일 수 없습니다.");
            throw new CustomException(ErrorCode.INVALID_FORMAT_HIREDATE);
        }

        UserEntity userEntity = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .hireDate(request.getHireDate())
                .position(UserPosition.fromString(request.getPosition()))
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();
        userRepository.save(userEntity);
    }

    // 사용자 중복체크
    @Override
    public void duplicateCheck(String type, String value) {
        switch (type) {
            case "username" -> {
                if (value.length() < 8 || value.length() > 20) {
                    throw new CustomException(ErrorCode.INVALID_LENGTH_USERNAME);
                }
                userRepository.findByUsername(value)
                        .ifPresent(user -> { throw new CustomException(ErrorCode.DUPLICATE_USERNAME); });
            }
            case "email" -> {
                if (!EMAIL_PATTERN.matcher(value).matches()) {
                    throw new CustomException(ErrorCode.INVALID_FORMAT_EMAIL);
                }
                userRepository.findByEmail(value)
                        .ifPresent(user -> { throw new CustomException(ErrorCode.DUPLICATE_EMAIL); });
            }
            default -> throw new CustomException(ErrorCode.INVALID_TYPE_VALUE);
        }
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .hireDate(user.getHireDate())
                .position(user.getPosition())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public Optional<UserEntity> findByUserId(Long userId) {
        return userRepository.findByUserId(userId);
    }
}
