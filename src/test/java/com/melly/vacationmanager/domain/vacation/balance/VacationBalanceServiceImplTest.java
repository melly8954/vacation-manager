package com.melly.vacationmanager.domain.vacation.balance;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.repository.VacationBalanceRepository;
import com.melly.vacationmanager.domain.vacation.balance.service.VacationBalanceServiceImpl;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VacationBalanceServiceImplTest {

    @Mock private VacationBalanceRepository vacationBalanceRepository;

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
}
