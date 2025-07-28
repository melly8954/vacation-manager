package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.user.dto.ProcessStatusRequest;
import com.melly.vacationmanager.domain.admin.user.service.AdminUserManageServiceImpl;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.domain.vacation.balance.service.IVacationBalanceService;
import com.melly.vacationmanager.domain.vacation.grant.service.IVacationGrantService;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeDto;
import com.melly.vacationmanager.domain.vacation.type.dto.VacationTypeListResponse;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.service.IVacationTypeService;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserManageServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private IVacationBalanceService vacationBalanceService;
    @Mock private IVacationGrantService vacationGrantService;
    @Mock private IVacationTypeService vacationTypeService;

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

            // 휴가 타입 리스트 stub
            List<VacationTypeDto> vacationTypeDtoList = List.of(
                    new VacationTypeDto("ANNUAL", "연차"),
                    new VacationTypeDto("SICK", "병가"),
                    new VacationTypeDto("FAMILY_EVENT", "경조사"),
                    new VacationTypeDto("SPECIAL", "특별휴가")
            );
            // grantVacation() 호출 시 예외처리를 반환하지 않고 Mock 정상 동작을 위한 stub
            when(vacationTypeService.getAllTypes())
                    .thenReturn(new VacationTypeListResponse(vacationTypeDtoList));
            // 각 타입에 대한 엔티티 stub
            for (VacationTypeDto dto : vacationTypeDtoList) {
                VacationTypeEntity entity = VacationTypeEntity.builder()
                        .typeCode(dto.getTypeCode())
                        .defaultDays(5)  // 예시 값
                        .build();
                when(vacationTypeService.findByTypeCode(dto.getTypeCode()))
                        .thenReturn(Optional.of(entity));
            }

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
            verifyNoInteractions(vacationTypeService, vacationBalanceService, vacationGrantService);
        }

        @Test
        @DisplayName("예외 흐름 - 잘못된 상태값으로 요청")
        void processPendingUsers_invalid_status() {
            // given
            Long userId = 1L;
            ProcessStatusRequest request = new ProcessStatusRequest("invalid");

            // when & then
            assertThatThrownBy(() -> adminUserManageService.processPendingUsers(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_STATUS);

            verifyNoInteractions(userRepository, vacationTypeService, vacationBalanceService, vacationGrantService);
        }

        @Test
        @DisplayName("예외 흐름 - 존재하지 않는 유저 ID")
        void processPendingUsers_user_not_found() {
            // given
            Long userId = 1L;
            ProcessStatusRequest request = new ProcessStatusRequest("approved");

            when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminUserManageService.processPendingUsers(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);

            verify(userRepository).findByUserId(userId);
            verifyNoMoreInteractions(userRepository);
            verifyNoInteractions(vacationTypeService, vacationBalanceService, vacationGrantService);
        }

        @Test
        @DisplayName("예외 흐름 - 휴가 타입 조회 실패 시 예외 발생")
        void processPendingUsers_vacationTypeNotFound_throwException() {
            // given
            Long userId = 1L;
            ProcessStatusRequest request = new ProcessStatusRequest("approved");

            List<VacationTypeDto> vacationTypeDtoList = List.of(
                    new VacationTypeDto("ANNUAL", "연차")
            );

            UserEntity user = UserEntity.builder()
                    .userId(userId)
                    .status(UserStatus.PENDING)
                    .build();

            when(userRepository.findByUserId(userId))
                    .thenReturn(Optional.of(user));
            when(vacationTypeService.getAllTypes())
                    .thenReturn(new VacationTypeListResponse(vacationTypeDtoList));
            when(vacationTypeService.findByTypeCode("ANNUAL"))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminUserManageService.processPendingUsers(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VACATION_TYPE_NOT_FOUND);

            verify(vacationTypeService).getAllTypes();
            verify(vacationTypeService).findByTypeCode("ANNUAL");
            verifyNoInteractions(vacationBalanceService, vacationGrantService);
        }
    }
}
