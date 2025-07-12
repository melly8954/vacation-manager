package com.melly.vacationmanager.domain.user.service;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.common.enums.UserPosition;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("사용자 가입 테스트")
    class SignUpTest {

        @Test
        @DisplayName("사용자 가입 정상 흐름")
        void success() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .username("testUser")
                    .password("1q2w3e4r!")
                    .confirmPassword("1q2w3e4r!")
                    .name("testUser")
                    .email("testUser@example.com")
                    .hireDate(LocalDate.of(2025,1,1))
                    .position(UserPosition.STAFF)
                    .build();
            given(passwordEncoder.encode(anyString())).willReturn("encodedPw");

            // when
            userService.signUp(request);

            // then
            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);  // 실제로 넘겨준 UserEntity 객체를 캡처해서 꺼내 볼 수 있게 해줌

            then(userRepository).should().save(captor.capture());       // userRepository.save()가 호출되었는지 확인하고, 호출 때 넘겨준 인자를 captor가 잡아냄

            UserEntity savedUser = captor.getValue();       // captor가 잡아낸 실제 저장 인스턴스를 변수에 저장
            assertEquals("testUser", savedUser.getUsername());
            assertEquals("encodedPw", savedUser.getPassword());
            assertEquals("testUser", savedUser.getName());
            assertEquals("testUser@example.com", savedUser.getEmail());
            assertEquals(LocalDate.of(2025,1,1), savedUser.getHireDate());
            assertEquals(UserPosition.STAFF, savedUser.getPosition());
            assertEquals(UserStatus.PENDING, savedUser.getStatus());
            assertEquals(UserRole.USER, savedUser.getRole());
        }

        @Test
        @DisplayName("사용자 가입 실패 흐름 - 비밀번호 불일치")
        void passwordMismatchException() {

        }
    }

}