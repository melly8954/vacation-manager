package com.melly.vacationmanager.domain.admin.service;

import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.VacationRequestStatusUpdateRequest;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.VacationRequestStatusUpdateResponse;
import com.melly.vacationmanager.domain.admin.vacation.request.service.AdminVacationRequestServiceImpl;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.auditlog.entity.VacationAuditLogEntity;
import com.melly.vacationmanager.domain.vacation.auditlog.repository.VacationAuditLogRepository;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.repository.VacationBalanceRepository;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import com.melly.vacationmanager.global.common.exception.CustomException;
import com.melly.vacationmanager.global.common.utils.CurrentUserUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminVacationRequestServiceImplTest {
    @Mock private VacationRequestRepository vacationRequestRepository;
    @Mock private VacationAuditLogRepository vacationAuditLogRepository;
    @Mock private VacationBalanceRepository vacationBalanceRepository;

    @InjectMocks
    private AdminVacationRequestServiceImpl adminVacationRequestService;

    private final Long requestId = 1L;
    private final Long userId = 100L;


    @Nested
    @DisplayName("updateVacationRequestStatus 메서드 테스트")
    class updateVacationRequestStatus {
        private MockedStatic<CurrentUserUtils> mockedStatic;

        @BeforeEach
        void setUp() {
            mockedStatic = mockStatic(CurrentUserUtils.class);
            when(CurrentUserUtils.getUser()).thenReturn(mock(UserEntity.class));
            when(CurrentUserUtils.getRole()).thenReturn(String.valueOf(UserRole.ADMIN));
        }

        @AfterEach
        void tearDown() {
            if (mockedStatic != null) mockedStatic.close();
        }

        @Test
        @DisplayName("정상 흐름 - 승인 처리 (APPROVED)")
        void updateStatus_approved_success() {
            // given
            VacationRequestEntity entity = createVacationRequestEntity();
            VacationBalanceEntity balance = createBalance(new BigDecimal("5.0"));

            when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
            when(vacationRequestRepository.existsApprovedOverlap(userId, entity.getStartDate(), entity.getEndDate()))
                    .thenReturn(false);
            when(vacationBalanceRepository.findById(any())).thenReturn(Optional.of(balance));

            VacationRequestStatusUpdateRequest request = new VacationRequestStatusUpdateRequest("APPROVED");

            // when
            VacationRequestStatusUpdateResponse response = adminVacationRequestService.updateVacationRequestStatus(requestId.toString(), request);

            // then
            assertThat(response.getRequestId()).isEqualTo(requestId);
            assertThat(response.getNewStatus()).isEqualTo("APPROVED");
            verify(vacationBalanceRepository).save(any(VacationBalanceEntity.class));
            verify(vacationAuditLogRepository).save(any(VacationAuditLogEntity.class));
        }

        @Test
        @DisplayName("정상 흐름 - 반려 처리 (REJECTED)")
        void updateStatus_rejected_success() {
            // given
            VacationRequestEntity entity = createVacationRequestEntity();

            when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
            VacationRequestStatusUpdateRequest request = new VacationRequestStatusUpdateRequest("REJECTED");

            // when
            VacationRequestStatusUpdateResponse response = adminVacationRequestService.updateVacationRequestStatus(requestId.toString(), request);

            // then
            assertThat(response.getRequestId()).isEqualTo(requestId);
            assertThat(response.getNewStatus()).isEqualTo("REJECTED");
            verify(vacationAuditLogRepository).save(any(VacationAuditLogEntity.class));
            verifyNoInteractions(vacationBalanceRepository); // 차감 없음
        }

        @Test
        @DisplayName("예외 흐름 - 존재하지 않는 휴가 신청")
        void updateStatus_notFound() {
            // given
            when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.empty());
            VacationRequestStatusUpdateRequest request = new VacationRequestStatusUpdateRequest("APPROVED");

            // when & then
            CustomException ex = assertThrows(CustomException.class, () -> {
                adminVacationRequestService.updateVacationRequestStatus(requestId.toString(), request);
            });

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VACATION_REQUEST_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 흐름 - 승인된 휴가신청 중복")
        void updateStatus_overlap_exception() {
            // given
            VacationRequestEntity entity = createVacationRequestEntity();
            when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
            when(vacationRequestRepository.existsApprovedOverlap(userId, entity.getStartDate(), entity.getEndDate()))
                    .thenReturn(true);
            VacationRequestStatusUpdateRequest request = new VacationRequestStatusUpdateRequest("APPROVED");

            // when & then
            CustomException ex = assertThrows(CustomException.class, () -> {
                adminVacationRequestService.updateVacationRequestStatus(requestId.toString(), request);
            });

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.OVERLAPPING_APPROVED_VACATION);
        }

        @Test
        @DisplayName("예외 흐름 - 휴가 잔여일 부족")
        void updateStatus_insufficient_balance() {
            // given
            VacationRequestEntity entity = createVacationRequestEntity();
            when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
            when(vacationRequestRepository.existsApprovedOverlap(any(), any(), any())).thenReturn(false);
            when(vacationBalanceRepository.findById(any())).thenReturn(Optional.of(createBalance(new BigDecimal("0.5")))); // 부족

            VacationRequestStatusUpdateRequest request = new VacationRequestStatusUpdateRequest("APPROVED");

            // when & then
            CustomException ex = assertThrows(CustomException.class, () -> {
                adminVacationRequestService.updateVacationRequestStatus(requestId.toString(), request);
            });

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 헬퍼 메서드
        private VacationRequestEntity createVacationRequestEntity() {
            UserEntity user = UserEntity.builder().userId(userId).build();
            return VacationRequestEntity.builder()
                    .requestId(requestId)
                    .user(user)
                    .vacationType(VacationTypeEntity.builder().typeCode("ANNUAL").build())
                    .status(VacationRequestStatus.PENDING)
                    .startDate(LocalDate.of(2025, 8, 1))
                    .endDate(LocalDate.of(2025, 8, 3))
                    .daysCount(new BigDecimal("2.0"))
                    .build();
        }

        private VacationBalanceEntity createBalance(BigDecimal remaining) {
            return VacationBalanceEntity.builder()
                    .id(new VacationBalanceId(userId, "ANNUAL"))
                    .remainingDays(remaining)
                    .build();
        }

    }

}
