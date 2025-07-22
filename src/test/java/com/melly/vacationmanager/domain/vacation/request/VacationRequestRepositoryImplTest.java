package com.melly.vacationmanager.domain.vacation.request;

import com.melly.vacationmanager.config.QueryDslTestConfig;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepositoryImpl;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslTestConfig.class)
@ActiveProfiles("test") // application-test.yml 설정을 적용
@DisplayName("중복된 휴가가 존재하는지 여부 확인")
public class VacationRequestRepositoryImplTest {
    @Autowired
    private VacationRequestRepositoryImpl vacationRequestRepositoryImpl;

    @Autowired
    private VacationRequestRepository vacationRequestRepository; // Spring Data JPA 인터페이스

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationTypeRepository vacationTypeRepository;

    @Test
    @DisplayName("승인된 휴가가 존재할 경우 -  true 반환")
    void existsApprovedOverlap_true() {
        // given
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .name("홍길동")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build());

        VacationTypeEntity type = vacationTypeRepository.save(VacationTypeEntity.builder()
                .typeCode("ANNUAL")
                .typeName("연차")
                .defaultDays(15)
                .build());

        VacationRequestEntity request = VacationRequestEntity.builder()
                .user(user)
                .vacationType(type)
                .startDate(LocalDate.of(2025, 7, 25))
                .endDate(LocalDate.of(2025, 7, 27))
                .daysCount(BigDecimal.valueOf(3))
                .reason("휴식")
                .status(VacationRequestStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        vacationRequestRepository.save(request);

        // when
        boolean result = vacationRequestRepositoryImpl.existsApprovedOverlap(
                user.getUserId(),
                LocalDate.of(2025, 7, 26), // 중간에 겹침
                LocalDate.of(2025, 7, 30)
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("승인된 휴가가 존재하지 않는 경우 - false 반환")
    void existsApprovedOverlap_false() {
        // given
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test2@example.com")
                .name("이몽룡")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build());

        VacationTypeEntity type = vacationTypeRepository.save(VacationTypeEntity.builder()
                .typeCode("SICK")
                .typeName("병가")
                .defaultDays(10)
                .build());

        VacationRequestEntity request = VacationRequestEntity.builder()
                .user(user)
                .vacationType(type)
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 3))
                .daysCount(BigDecimal.valueOf(3))
                .reason("병원")
                .status(VacationRequestStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();

        vacationRequestRepository.save(request);

        // when
        boolean result = vacationRequestRepositoryImpl.existsApprovedOverlap(
                user.getUserId(),
                LocalDate.of(2025, 7, 10),
                LocalDate.of(2025, 7, 12)
        );

        // then
        assertThat(result).isFalse();
    }
}
