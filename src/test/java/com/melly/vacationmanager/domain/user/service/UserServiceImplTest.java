package com.melly.vacationmanager.domain.user.service;

import com.melly.vacationmanager.domain.user.dto.request.SignUpRequest;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.UserPosition;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
            SignUpRequest request = createSignUpRequest("1q2w3e4r!", "1q2w3e4r!");
            given(passwordEncoder.encode(anyString())).willReturn("encodedPw");

            // when
            userService.signUp(request);

            // then
            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);  // 실제로 넘겨준 UserEntity 객체를 캡처해서 꺼내 볼 수 있게 해줌

            then(userRepository).should().save(captor.capture());       // userRepository.save()가 호출되었는지 확인하고, 호출 때 넘겨준 인자를 captor가 잡아냄

            UserEntity savedUser = captor.getValue();       // captor가 잡아낸 실제 저장 인스턴스를 변수에 저장
            assertThat(savedUser.getUsername()).isEqualTo("testUser");
            assertThat(savedUser.getPassword()).isEqualTo("encodedPw");
            assertThat(savedUser.getName()).isEqualTo("testUser");
            assertThat(savedUser.getEmail()).isEqualTo("testUser@example.com");
            assertThat(savedUser.getHireDate()).isEqualTo(LocalDate.now().minusDays(1));
            assertThat(savedUser.getPosition()).isEqualTo(UserPosition.STAFF);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("사용자 가입 실패 흐름 - 비밀번호 불일치")
        void passwordMismatchException() {
            // given
            SignUpRequest request = createSignUpRequest("1q2w3e4r!!", "1q2w3e4r!@");
            // 예외는 암호화 전에 발생하여 PasswordEncoder는 호출되지도 않음

            // when & then
//            // 기본 Junit5 예외처리 + AssertJ
//            CustomException e = assertThrows(CustomException.class, () -> userService.signUp(request));
//            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH);
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PASSWORD_MISMATCH);
        }

        @Test
        @DisplayName("사용자 가입 실패 흐름 - 입사일 유효성 검사 실패")
        void invalidHireDate() {
            // given
            SignUpRequest request = createSignUpRequest("1q2w3e4r!","1q2w3e4r!",LocalDate.now().plusDays(1));

            // when & then
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_FORMAT_HIREDATE);
        }
    }

    private SignUpRequest createSignUpRequest(String password, String confirmPassword) {
        return SignUpRequest.builder()
                .username("testUser")
                .password(password)
                .confirmPassword(confirmPassword)
                .name("testUser")
                .email("testUser@example.com")
                .hireDate(LocalDate.now().minusDays(1))
                .position(UserPosition.STAFF)
                .build();
    }
    // 오버로딩
    private SignUpRequest createSignUpRequest(String password, String confirmPassword, LocalDate hireDate) {
        return SignUpRequest.builder()
                .username("testUser")
                .password(password)
                .confirmPassword(confirmPassword)
                .name("testUser")
                .email("testUser@example.com")
                .hireDate(hireDate)
                .position(UserPosition.STAFF)
                .build();
    }

    @Nested
    @DisplayName("사용자 중복 검사 테스트")
    class duplicateCheckTest {
        @Test
        @DisplayName("사용자 중복 검사 정상 흐름 - 아이디")     // 정상 흐름은 중복이 존재하지 않는 상황이므로, 예외가 발생하지 않아야 하는 흐름이다.
        void duplicateCheck_noDuplicate_username() {
            // given
            String type = "username";
            String value = "testUser";
            given(userRepository.findByUsername(value)).willReturn(Optional.empty());

            // when & then (예외 발생하지 않아야 통과)
            assertThatCode(() -> userService.duplicateCheck(type, value))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("사용자 중복 검사 정상 흐름 - 이메일")
        void duplicateCheck_noDuplicate_email() {
            // given
            String type = "email";
            String value = "testUser@example.com";
            given(userRepository.findByEmail(value)).willReturn(Optional.empty());

            // when & then (예외 발생하지 않아야 통과)
            assertThatCode(() -> userService.duplicateCheck(type, value))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("사용자 중복 검사 실패 흐름 - 아이디 중복")
        void duplicateCheck_duplicate_username() {
            // given
            String type = "username";
            String value = "testUser";
            UserEntity dummyUser = UserEntity.builder()
                    .username(value)
                    .password("encodedPw")
                    .name("testUser")
                    .email("testUser@example.com")
                    .hireDate(LocalDate.now().minusDays(1))
                    .position(UserPosition.STAFF)
                    .build();
            given(userRepository.findByUsername(value)).willReturn(Optional.of(dummyUser));  // 존재한다고 가정 -> 예외 발생 유도

            // when & then
            assertThatThrownBy(() -> userService.duplicateCheck(type, value))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_USERNAME);
        }

        @Test
        @DisplayName("사용자 중복 검사 실패 흐름 - 이메일 중복")
        void duplicateCheck_duplicate_email() {
            // given
            String type = "email";
            String value = "testUser@example.com";
            UserEntity dummyUser = UserEntity.builder()
                    .username("testUser")
                    .password("encodedPw")
                    .name("testUser")
                    .email(value)
                    .hireDate(LocalDate.now().minusDays(1))
                    .position(UserPosition.STAFF)
                    .build();
            given(userRepository.findByEmail(value)).willReturn(Optional.of(dummyUser));  // 존재한다고 가정 -> 예외 발생 유도

            // when & then
            assertThatThrownBy(() -> userService.duplicateCheck(type, value))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        }
    }

}