package com.melly.vacationmanager.domain.vacation.grant.repository;

import com.melly.vacationmanager.config.QueryDslTestConfig;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;
import com.melly.vacationmanager.domain.vacation.grant.entity.VacationGrantEntity;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslTestConfig.class)
@ActiveProfiles("test") // application-test.yml 설정을 적용
@DisplayName("VacationGrantRepositoryImpl 쿼리 테스트")
public class VacationGrantRepositoryImplTest {
    @Autowired
    private VacationGrantRepository vacationGrantRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("findGrantStatisticsBetween 쿼리 테스트")
    class findGrantStatisticsBetween {
        private VacationTypeEntity annualType;
        private VacationTypeEntity sickType;

        @BeforeEach
        void setUp() {
            annualType = VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .typeName("연차")
                    .build();

            sickType = VacationTypeEntity.builder()
                    .typeCode("SICK")
                    .typeName("병가")
                    .build();

            em.persist(annualType);
            em.persist(sickType);

            em.persist(VacationGrantEntity.builder()
                    .type(annualType)
                    .grantDate(LocalDate.of(2025, 1, 10))
                    .grantedDays(5)
                    .build());

            em.persist(VacationGrantEntity.builder()
                    .type(annualType)
                    .grantDate(LocalDate.of(2025, 1, 20))
                    .grantedDays(3)
                    .build());

            em.persist(VacationGrantEntity.builder()
                    .type(sickType)
                    .grantDate(LocalDate.of(2025, 1, 15))
                    .grantedDays(2)
                    .build());

            em.persist(VacationGrantEntity.builder()
                    .type(annualType)
                    .grantDate(LocalDate.of(2024, 12, 31))
                    .grantedDays(7)
                    .build());
        }

        @Test
        @DisplayName("연도/월 별 휴가 지급 합계 조회")
        void testFindGrantStatisticsBetween() {
            // given
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 31);

            // when
            List<VacationGrantStatisticsResponse> result =
                    vacationGrantRepository.findGrantStatisticsBetween(start, end);

            // then
            assertThat(result).hasSize(2);

            VacationGrantStatisticsResponse annual = result.stream()
                    .filter(r -> r.getTypeCode().equals("ANNUAL"))
                    .findFirst()
                    .orElseThrow();

            VacationGrantStatisticsResponse sick = result.stream()
                    .filter(r -> r.getTypeCode().equals("SICK"))
                    .findFirst()
                    .orElseThrow();

            assertThat(annual.getTotalGrantedDays()).isEqualByComparingTo("8");
            assertThat(sick.getTotalGrantedDays()).isEqualByComparingTo("2");
        }
    }

}
