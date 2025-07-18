package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.dto.request.ProcessStatusRequest;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.domain.user.service.UserServiceImpl;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.domain.vacation.grant.service.IVacationGrantService;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserManageServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private IVacationBalanceService vacationBalanceService;

    @Mock
    private IVacationGrantService vacationGrantService;

    @Mock
    private VacationTypeRepository vacationTypeRepository;

    @InjectMocks
    private AdminUserManageServiceImpl adminUserManageService;


    @Nested
    @DisplayName("관리자 사용자 가입 승인대기 처리 테스트")
    class processPendingUsers {
        @Test
        @DisplayName("정상 흐름 - 가입 승인 처리")
        void processPendingUsers_approved_success() {
            // given
            Long userId = 1L;
            ProcessStatusRequest request = new ProcessStatusRequest("approved");

            UserEntity user = UserEntity.builder()
                    .userId(userId)
                    .status(UserStatus.PENDING)
                    .build();
            when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

            // grantVacation() 호출 시 예외처리를 반환하지 않고 Mock 정상 동작을 위한 stub
            when(vacationTypeRepository.findByTypeCode("ANNUAL"))
                    .thenReturn(Optional.of(VacationTypeEntity.builder().typeCode("ANNUAL").build()));

            when(vacationTypeRepository.findByTypeCode("SICK"))
                    .thenReturn(Optional.of(VacationTypeEntity.builder().typeCode("SICK").build()));

            when(vacationTypeRepository.findByTypeCode("FAMILY_EVENT"))
                    .thenReturn(Optional.of(VacationTypeEntity.builder().typeCode("FAMILY_EVENT").build()));

            when(vacationTypeRepository.findByTypeCode("SPECIAL"))
                    .thenReturn(Optional.of(VacationTypeEntity.builder().typeCode("SPECIAL").build()));

            // when
            adminUserManageService.processPendingUsers(userId, request);

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

            verify(userRepository).save(user);  // 호출 확인
            verify(vacationBalanceService, times(4)).initializeVacationBalance(eq(user), any(), any());     // eq(user)는 같은 객체가 들어가는지 정확히 검증
            verify(vacationGrantService, times(3)).recordGrant(eq(user), any(), anyInt());
        }

        @Test
        @DisplayName("정상 흐름 - 가입 반려 처리")
        void processPendingUsers_rejected_success() {
            // given
            Long userId = 1L;
            ProcessStatusRequest request = new ProcessStatusRequest("rejected");

            UserEntity user = UserEntity.builder()
                    .userId(userId)
                    .status(UserStatus.PENDING)
                    .build();
            when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

            // when
            adminUserManageService.processPendingUsers(userId, request);

            // then
            verify(userRepository).save(user);
            assertThat(user.getStatus()).isEqualTo(UserStatus.REJECTED);

            // 휴가 지급 관련 메서드 호출이 없어야 함
            verifyNoInteractions(vacationBalanceService);
            verifyNoInteractions(vacationGrantService);
        }
    }
}
