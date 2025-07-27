package com.melly.vacationmanager.domain.vacation.request;

import com.melly.vacationmanager.config.QueryDslTestConfig;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestListResponse;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepository;
import com.melly.vacationmanager.domain.vacation.request.repository.VacationRequestRepositoryImpl;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.global.common.enums.UserRole;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslTestConfig.class)
@ActiveProfiles("test") // application-test.yml 설정을 적용
@DisplayName("VacationRequestRepositoryImpl 기능 테스트")
public class VacationRequestRepositoryImplTest {
    @Autowired
    private VacationRequestRepositoryImpl vacationRequestRepositoryImpl;

    @Autowired
    private VacationRequestRepository vacationRequestRepository; // Spring Data JPA 인터페이스

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacationTypeRepository vacationTypeRepository;

    @Nested
    @DisplayName("existsApprovedOverlap 메서드 테스트")
    class ExistsApprovedOverlapTest {
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

    @Nested
    @DisplayName("findMyVacationRequests 조건 필터 테스트 (기본 정렬 내림차순)")
    class FindMyVacationRequestsTest {
        // 공통 테스트 데이터 세팅
        private UserEntity user;
        private VacationTypeEntity annual;
        private VacationTypeEntity sick;

        @BeforeEach
        void setUp() {
            user = userRepository.save(UserEntity.builder()
                    .email("user@example.com")
                    .name("테스트유저")
                    .status(UserStatus.ACTIVE)
                    .role(UserRole.USER)
                    .build());
            annual = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .typeName("연차")
                    .defaultDays(15)
                    .build());
            sick = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("SICK")
                    .typeName("병가")
                    .defaultDays(10)
                    .build());

            saveVacation(LocalDate.of(2025, 6, 1), 3, annual, VacationRequestStatus.REJECTED);
            saveVacation(LocalDate.of(2025, 7, 10), 1, annual, VacationRequestStatus.APPROVED);
            saveVacation(LocalDate.of(2025, 1, 10), 1, sick, VacationRequestStatus.PENDING);
            saveVacation(LocalDate.of(2025, 5, 1), 2, annual, VacationRequestStatus.APPROVED);
        }

        private void saveVacation(LocalDate startDate, int days, VacationTypeEntity type, VacationRequestStatus status) {
            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .user(user)
                    .vacationType(type)
                    .startDate(startDate)
                    .endDate(startDate.plusDays(days - 1))
                    .daysCount(BigDecimal.valueOf(days))
                    .status(status)
                    .reason("테스트용")
                    .createdAt(LocalDateTime.now().minusDays((long) (Math.random() * 100)))
                    .build());
        }

        // cond 객체를 생성하는 헬퍼 메서드
        private VacationRequestSearchCond createCond(
                Long userId,
                String typeCode,
                String status,
                String year,
                String month,
                String order
        ) {
            return VacationRequestSearchCond.builder()
                    .userId(userId)
                    .typeCode(typeCode)
                    .status(status)
                    .year(year)
                    .month(month)
                    .order(order)
                    .build();
        }

        @Test
        @DisplayName("모든 조건 ALL - 전체 조회")
        void testFindAllFilters() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "ALL",
                    "ALL",
                    "ALL",
                    "desc"
            );

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("상태 필터만 적용 (PENDING)")
        void testFindByStatus() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "PENDING",
                    "ALL",
                    "ALL",
                    "desc"
            );

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getStatus() == VacationRequestStatus.PENDING);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("유형 필터만 적용 (ANNUAL)")
        void testFindByType() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ANNUAL",
                    "ALL",
                    "ALL",
                    "ALL",
                    "desc"
            );

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getTypeCode().equals("ANNUAL"));
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("연도 필터만 적용 (2025)")
        void testFindByYear() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "ALL",
                    "2025",
                    "ALL",
                    "desc"
            );

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getStartDate().getYear() == 2025);
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("월 필터만 적용 (7월)")
        void testFindByMonth() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "ALL",
                    "ALL",
                    "7",
                    "desc"
            );

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getStartDate().getMonthValue() == 7);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태 + 유형 + 연도 + 월 필터 복합 적용")
        void testFindByMultipleFilters() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ANNUAL",
                    "REJECTED",
                    "2025",
                    "6",
                    "desc"
            );

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v ->
                    v.getTypeCode().equals("ANNUAL") &&
                            v.getStatus() == VacationRequestStatus.REJECTED &&
                            v.getStartDate().getYear() == 2025 &&
                            v.getStartDate().getMonthValue() == 6
            );
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("등록일 기준 내림차순 정렬")
        void testSortByCreatedAtDesc() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "ALL",
                    "ALL",
                    "ALL",
                    "desc"
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent())
                    .isSortedAccordingTo((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));  // 내림차순 검증
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("등록일 기준 오름차순 정렬")
        void testSortByCreatedAtAsc() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "ALL",
                    "ALL",
                    "ALL",
                    "asc"
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent())
                    .isSortedAccordingTo((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));  // 오름차순 검증
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("페이징 처리 - 첫 페이지 조회")
        void testPagingFirstPage() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "ALL",
                    "ALL",
                    "ALL",
                    "desc"
            );

            Pageable pageable = PageRequest.of(0, 3);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(4);  // 전체 데이터 개수는 4건
        }

        @Test
        @DisplayName("페이징 처리 - 두 번째 페이지 조회")
        void testPagingSecondPage() {
            VacationRequestSearchCond cond = createCond(
                    user.getUserId(),
                    "ALL",
                    "ALL",
                    "ALL",
                    "ALL",
                    "desc"
            );

            Pageable pageable = PageRequest.of(1, 3);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(4);  // 전체 데이터 개수는 4건
        }
    }
}
