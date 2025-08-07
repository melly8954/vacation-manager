package com.melly.vacationmanager.domain.vacation.balance;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.balance.dto.VacationBalanceListResponse;
import com.melly.vacationmanager.domain.vacation.balance.dto.VacationBalanceResponse;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.repository.VacationBalanceRepository;
import com.melly.vacationmanager.domain.vacation.balance.service.VacationBalanceServiceImpl;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VacationBalanceServiceImplTest {

    @Mock private VacationBalanceRepository vacationBalanceRepository;
    @Mock private VacationGrantRepository vacationGrantRepository;
    @Mock private VacationRequestRepository vacationRequestRepository;

    @InjectMocks
    private VacationBalanceServiceImpl vacationBalanceService;

    @Test
    @DisplayName("정상 흐름 - 휴가 잔여 일수 초기화 (신규 잔여 일수 생성 및 저장)")
    void InitializeVacationBalance_whenNotExist_thenCreateAndSave() {
        // given
        UserEntity user = UserEntity.builder()
                .userId(1L)
                .build();

        VacationTypeEntity type = VacationTypeEntity.builder()
                .typeCode("ANNUAL")
                .build();

        VacationBalanceId expectedId = new VacationBalanceId(user.getUserId(), type.getTypeCode());

        when(vacationBalanceRepository.existsById(expectedId)).thenReturn(false);

        // when
        vacationBalanceService.initializeVacationBalance(user, type, 5);

        // then
        verify(vacationBalanceRepository).existsById(expectedId);
        verify(vacationBalanceRepository).save(argThat(balance ->
                balance.getId().equals(expectedId) &&
                        balance.getUser().equals(user) &&
                        balance.getType().equals(type) &&
                        balance.getRemainingDays().compareTo(BigDecimal.valueOf(5)) == 0     // compareTo(다른값) 이 반환하는 값이 0이면 두 값이 동일
        ));
    }

    @Nested
    @DisplayName("getVacationBalancesByUserId() 메서드 테스트")
    class getVacationBalancesByUserIdTests {
        private final Long userId = 1L;

        @Test
        @DisplayName("정상 흐름 - 여러 휴가 유형의 잔여일 조회")
        void testGetVacationBalancesByUserId_multipleTypes() {
            // given - 여러 휴가 유형 엔티티 생성
            VacationBalanceEntity annual = createVacationBalanceEntity("ANNUAL", "연차", BigDecimal.valueOf(10.5));
            VacationBalanceEntity sick = createVacationBalanceEntity("SICK", "병가", BigDecimal.valueOf(3));
            VacationBalanceEntity familyEvent = createVacationBalanceEntity("FAMILY_EVENT", "경조사", BigDecimal.valueOf(0));
            VacationBalanceEntity special = createVacationBalanceEntity("SPECIAL", "특별휴가", BigDecimal.valueOf(3));

            List<VacationBalanceEntity> entities = List.of(annual, sick, familyEvent, special);
            when(vacationBalanceRepository.findByUser_UserId(userId)).thenReturn(entities);

            // 각 유형별 grantedDays, usedDays 모킹
            when(vacationGrantRepository.sumGrantedDays(userId, "ANNUAL")).thenReturn(BigDecimal.valueOf(15));
            when(vacationRequestRepository.sumUsedDays(userId, "ANNUAL", VacationRequestStatus.APPROVED)).thenReturn(BigDecimal.valueOf(4.5));

            when(vacationGrantRepository.sumGrantedDays(userId, "SICK")).thenReturn(BigDecimal.valueOf(10));
            when(vacationRequestRepository.sumUsedDays(userId, "SICK", VacationRequestStatus.APPROVED)).thenReturn(BigDecimal.valueOf(7));

            when(vacationGrantRepository.sumGrantedDays(userId, "FAMILY_EVENT")).thenReturn(BigDecimal.valueOf(3));
            when(vacationRequestRepository.sumUsedDays(userId, "FAMILY_EVENT", VacationRequestStatus.APPROVED)).thenReturn(BigDecimal.valueOf(3));

            when(vacationGrantRepository.sumGrantedDays(userId, "SPECIAL")).thenReturn(BigDecimal.valueOf(5));
            when(vacationRequestRepository.sumUsedDays(userId, "SPECIAL", VacationRequestStatus.APPROVED)).thenReturn(BigDecimal.valueOf(2));

            // when
            VacationBalanceListResponse response = vacationBalanceService.getVacationBalancesByUserId(userId);
            List<VacationBalanceResponse> result = response.getVacationBalances();

            // then
            assertThat(result).hasSize(4);

            // 각각의 휴가 유형에 대해 검증
            VacationBalanceResponse annualResp = result.stream()
                    .filter(r -> r.getTypeCode().equals("ANNUAL"))
                    .findFirst()
                    .orElseThrow();
            assertThat(annualResp.getTypeName()).isEqualTo("연차");
            assertThat(annualResp.getGrantedDays()).isEqualByComparingTo(BigDecimal.valueOf(15));
            assertThat(annualResp.getUsedDays()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
            assertThat(annualResp.getRemainingDays()).isEqualByComparingTo(BigDecimal.valueOf(10.5));

            VacationBalanceResponse sickResp = result.stream()
                    .filter(r -> r.getTypeCode().equals("SICK"))
                    .findFirst()
                    .orElseThrow();
            assertThat(sickResp.getTypeName()).isEqualTo("병가");
            assertThat(sickResp.getGrantedDays()).isEqualByComparingTo(BigDecimal.valueOf(10));
            assertThat(sickResp.getUsedDays()).isEqualByComparingTo(BigDecimal.valueOf(7));
            assertThat(sickResp.getRemainingDays()).isEqualByComparingTo(BigDecimal.valueOf(3));

            VacationBalanceResponse familyEventResp = result.stream()
                    .filter(r -> r.getTypeCode().equals("FAMILY_EVENT"))
                    .findFirst()
                    .orElseThrow();
            assertThat(familyEventResp.getTypeName()).isEqualTo("경조사");
            assertThat(familyEventResp.getGrantedDays()).isEqualByComparingTo(BigDecimal.valueOf(3));
            assertThat(familyEventResp.getUsedDays()).isEqualByComparingTo(BigDecimal.valueOf(3));
            assertThat(familyEventResp.getRemainingDays()).isEqualByComparingTo(BigDecimal.valueOf(0));

            VacationBalanceResponse specialResp = result.stream()
                    .filter(r -> r.getTypeCode().equals("SPECIAL"))
                    .findFirst()
                    .orElseThrow();
            assertThat(specialResp.getTypeName()).isEqualTo("특별휴가");
            assertThat(specialResp.getGrantedDays()).isEqualByComparingTo(BigDecimal.valueOf(5));
            assertThat(specialResp.getUsedDays()).isEqualByComparingTo(BigDecimal.valueOf(2));
            assertThat(specialResp.getRemainingDays()).isEqualByComparingTo(BigDecimal.valueOf(3));

            // repository 호출 횟수 검증
            verify(vacationGrantRepository, times(1)).sumGrantedDays(userId, "ANNUAL");
            verify(vacationGrantRepository, times(1)).sumGrantedDays(userId, "SICK");
            verify(vacationGrantRepository, times(1)).sumGrantedDays(userId, "FAMILY_EVENT");
            verify(vacationGrantRepository, times(1)).sumGrantedDays(userId, "SPECIAL");

            verify(vacationRequestRepository, times(1)).sumUsedDays(userId, "ANNUAL", VacationRequestStatus.APPROVED);
            verify(vacationRequestRepository, times(1)).sumUsedDays(userId, "SICK", VacationRequestStatus.APPROVED);
            verify(vacationRequestRepository, times(1)).sumUsedDays(userId, "FAMILY_EVENT", VacationRequestStatus.APPROVED);
            verify(vacationRequestRepository, times(1)).sumUsedDays(userId, "SPECIAL", VacationRequestStatus.APPROVED);
        }

        @Test
        @DisplayName("정상 흐름 - 조회 결과가 없는 경우 빈 리스트 반환")
        void testGetVacationBalancesByUserId_noData() {
            // given
            when(vacationBalanceRepository.findByUser_UserId(userId))
                    .thenReturn(Collections.emptyList());

            // when
            VacationBalanceListResponse response = vacationBalanceService.getVacationBalancesByUserId(userId);
            List<VacationBalanceResponse> result = response.getVacationBalances();

            // then
            assertThat(result).isEmpty();
        }

        // 헬퍼 메서드 - 엔티티 생성
        private VacationBalanceEntity createVacationBalanceEntity(String typeCode, String typeName, BigDecimal remainingDays) {
            VacationTypeEntity typeEntity = VacationTypeEntity.builder()
                    .typeCode(typeCode)
                    .typeName(typeName)
                    .build();

            VacationBalanceId id = new VacationBalanceId();
            id.setTypeCode(typeCode);

            return VacationBalanceEntity.builder()
                    .id(id)
                    .type(typeEntity)
                    .remainingDays(remainingDays)
                    .build();
        }
    }
}
