package com.melly.vacationmanager.domain.vacation.grant.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.grant.entity.VacationGrantEntity;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class VacationGrantServiceImplTest {
    @Mock
    private VacationGrantRepository vacationGrantRepository;

    @InjectMocks
    private VacationGrantServiceImpl vacationGrantService;

    @Test
    @DisplayName("정상 흐름 - 휴가 부여 기록이 정상적으로 저장")
     void record_vacation_successful_granted() {
        // given
        UserEntity user = UserEntity.builder()
                .userId(1L)
                .build();
        VacationTypeEntity type = VacationTypeEntity.builder()
                .typeCode("ANNUAL")
                .build();
        int days = 3;

        // when
        vacationGrantService.recordGrant(user, type, days);

        // then
        ArgumentCaptor<VacationGrantEntity> captor = ArgumentCaptor.forClass(VacationGrantEntity.class);
        verify(vacationGrantRepository, times(1)).save(captor.capture());

        VacationGrantEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getUser()).isEqualTo(user);
        assertThat(savedEntity.getType()).isEqualTo(type);
        assertThat(savedEntity.getGrantedDays()).isEqualTo(days);
        assertThat(savedEntity.getGrantDate()).isEqualTo(LocalDate.now());
    }

    @Nested
    @DisplayName("grantRegularVacations() 메서드 테스트")
    class grantRegularVacationsTest {
        @Test
        @DisplayName("입사 1년 미만: 입사월 기준 남은 달 수 만큼 비례 연차 지급")
        void calculateDaysToGrant_under1year() {
            // given
            LocalDate today = LocalDate.of(2025, 8, 1);
            LocalDate hireDate = LocalDate.of(2025, 3, 1); // 3월 입사

            UserEntity user = UserEntity.builder()
                    .userId(1L)
                    .hireDate(hireDate)
                    .build();

            VacationTypeEntity type = VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .build();

            // when
            int granted = vacationGrantService.calculateDaysToGrant(user, type);

            // then
            assertThat(granted).isEqualTo(10);
        }

        @Test
        @DisplayName("입사 3년 이상: 기본 15일 + 1일 가산")
        void calculateDaysToGrant_3years_plus() {
            UserEntity user = UserEntity.builder()
                    .userId(1L)
                    .hireDate(LocalDate.now().minusYears(3).minusDays(1))
                    .build();

            VacationTypeEntity type = VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .build();

            int granted = vacationGrantService.calculateDaysToGrant(user, type);

            assertThat(granted).isEqualTo(16);
        }

        @Test
        @DisplayName("입사 7년 이상: 기본 15일 + 3일 가산 (최대)")
        void calculateDaysToGrant_7years_plus() {
            UserEntity user = UserEntity.builder()
                    .userId(1L)
                    .hireDate(LocalDate.now().minusYears(10))
                    .build();

            VacationTypeEntity type = VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .build();

            int granted = vacationGrantService.calculateDaysToGrant(user, type);

            assertThat(granted).isEqualTo(18);
        }

        @Test
        @DisplayName("연차 이외 휴가: 기본 일수로 지급")
        void calculateDaysToGrant_nonAnnual() {
            UserEntity user = UserEntity.builder()
                    .userId(1L)
                    .hireDate(LocalDate.now().minusYears(1))
                    .build();

            VacationTypeEntity type = VacationTypeEntity.builder()
                    .typeCode("SICK")
                    .defaultDays(10)
                    .build();

            int granted = vacationGrantService.calculateDaysToGrant(user, type);

            assertThat(granted).isEqualTo(10);
        }
    }
}
