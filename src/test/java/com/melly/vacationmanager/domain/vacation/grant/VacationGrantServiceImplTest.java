package com.melly.vacationmanager.domain.vacation.grant;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.grant.entity.VacationGrantEntity;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.grant.service.VacationGrantServiceImpl;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import org.junit.jupiter.api.DisplayName;
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
}
