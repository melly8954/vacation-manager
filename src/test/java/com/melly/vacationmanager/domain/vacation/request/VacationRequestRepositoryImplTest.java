package com.melly.vacationmanager.domain.vacation.request;

import com.melly.vacationmanager.config.QueryDslTestConfig;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestListResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.MonthlyVacationUsageResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationStatusChangeStatisticsResponse;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationUsageStatisticsRaw;
import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationUsageStatisticsResponse;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationCalendarResponse;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslTestConfig.class)
@ActiveProfiles("test") // application-test.yml 설정을 적용
@DisplayName("VacationRequestRepositoryImpl 쿼리 테스트")
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
    @DisplayName("existsApprovedOverlap 쿼리 테스트")
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
    @DisplayName("findMyVacationRequests 쿼리 테스트")
    class FindMyVacationRequestsTest {
        // 공통 테스트 데이터 세팅
        private UserEntity user;

        @BeforeEach
        void setUp() {
            user = userRepository.save(UserEntity.builder()
                    .email("user@example.com")
                    .name("테스트유저")
                    .status(UserStatus.ACTIVE)
                    .role(UserRole.USER)
                    .build());
            VacationTypeEntity annual = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .typeName("연차")
                    .defaultDays(15)
                    .build());
            VacationTypeEntity sick = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("SICK")
                    .typeName("병가")
                    .defaultDays(10)
                    .build());

            // 휴가 신청 4건
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
                    .createdAt(LocalDateTime.of(2025, 7, 15, 10, 0))
                    .build());
        }

        @Test
        @DisplayName("전체 조건 적용 (ALL)")
        void testFindAllFilters() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("상태 필터(PENDING) 적용")
        void testFindByStatus() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("PENDING")
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .page(0)
                    .size(10)
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getStatus() == VacationRequestStatus.PENDING);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("유형 필터(ANNUAL) 적용")
        void testFindByType() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ANNUAL")
                    .status("ALL")
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .page(0)
                    .size(10)
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getTypeCode().equals("ANNUAL"));
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("연도 필터(2025) 적용")
        void testFindByYear() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("2025")
                    .month("ALL")
                    .order("desc")
                    .page(0)
                    .size(10)
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getCreatedAt().getYear() == 2025);
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("월 필터(7월) 적용")
        void testFindByMonth() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("ALL")
                    .month("7")
                    .order("desc")
                    .page(0)
                    .size(10)
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).allMatch(v -> v.getCreatedAt().getMonthValue() == 7);
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("복합 필터 적용 (ANNUAL, REJECTED, 2025, 6월)")
        void testFindByMultipleFilters() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ANNUAL")
                    .status("REJECTED")
                    .year("2025")
                    .month("6")
                    .order("desc")
                    .page(0)
                    .size(10)
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).allMatch(v ->
                    v.getTypeCode().equals("ANNUAL") &&
                            v.getStatus() == VacationRequestStatus.REJECTED &&
                            v.getCreatedAt().getYear() == 2025 &&
                            v.getCreatedAt().getMonthValue() == 6
            );
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("정렬 조건(desc) 적용 - createdAt 내림차순으로 정렬")
        void testSortByCreatedAtDesc() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .page(0)
                    .size(10)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent())
                    .isSortedAccordingTo((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));  // 내림차순 검증
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("정렬 조건(asc) 적용 - createdAt 오름차순으로 정렬")
        void testSortByCreatedAtAsc() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("ALL")
                    .month("ALL")
                    .order("asc")
                    .page(0)
                    .size(10)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent())
                    .isSortedAccordingTo((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));  // 오름차순 검증
            assertThat(result.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("페이징 처리 (page=0, size=3) - 첫 페이지 조회")
        void testPagingFirstPage() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 3);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(4);  // 전체 데이터 개수는 4건
        }

        @Test
        @DisplayName("페이징 처리 (page=1, size=3) - 두 번째 페이지 조회")
        void testPagingSecondPage() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(1, 3);
            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl.findMyVacationRequests(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(3);
            assertThat(result.getTotalElements()).isEqualTo(4);  // 전체 데이터 개수는 4건
        }

        @Test
        @DisplayName("[dateFilterType == vacationPeriod] 휴가기간 필터 적용 - 2025년 6월")
        void testVacationPeriodFilterByYearAndMonth() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("2025")
                    .month("6")
                    .order("desc")
                    .dateFilterType("vacationPeriod")
                    .page(0)
                    .size(10)
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl
                    .findMyVacationRequests(cond, PageRequest.of(cond.getPage(), cond.getSize()));

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getStartDate().getMonthValue()).isEqualTo(6);
        }

        @Test
        @DisplayName("[dateFilterType == vacationPeriod] 휴가기간 필터 적용 - 모든 연도 7월")
        void testVacationPeriodFilterByMonthOnly() {
            VacationRequestSearchCond cond = VacationRequestSearchCond.builder()
                    .userId(user.getUserId())
                    .typeCode("ALL")
                    .status("ALL")
                    .year("ALL")
                    .month("7")
                    .order("desc")
                    .dateFilterType("vacationPeriod")
                    .page(0)
                    .size(10)
                    .build();

            Page<VacationRequestListResponse> result = vacationRequestRepositoryImpl
                    .findMyVacationRequests(cond, PageRequest.of(cond.getPage(), cond.getSize()));

            assertThat(result).isNotNull();
            assertThat(result.getContent())
                    .allMatch(v -> {
                        LocalDate start = v.getStartDate();
                        LocalDate end = v.getEndDate();
                        return start.getMonthValue() <= 7 && end.getMonthValue() >= 7;
                    });
        }
    }

    @Nested
    @DisplayName("findAllVacationRequestsForAdmin 쿼리 테스트")
    class findAllVacationRequestsForAdminTest {
        // 공통 테스트 데이터 세팅
        UserEntity user1;
        UserEntity user2;
        VacationTypeEntity annual;
        VacationTypeEntity sick;

        @BeforeEach
        void setUp() {
            user1 = userRepository.save(UserEntity.builder()
                    .username("user1")
                    .name("선우")
                    .status(UserStatus.ACTIVE)
                    .build());

            user2 = userRepository.save(UserEntity.builder()
                    .username("user2")
                    .name("김철수")
                    .status(UserStatus.ACTIVE)
                    .build());

            annual = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .typeName("연차")
                    .build());

            sick = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("SICK")
                    .typeName("병가")
                    .build());

            // 휴가 신청 3건
            saveVacation(user1, LocalDate.of(2025, 7, 1), 1, annual, VacationRequestStatus.APPROVED, "사유1",
                    LocalDateTime.of(2025, 7, 1, 9, 0));

            saveVacation(user1, LocalDate.of(2025, 7, 5), 1, sick, VacationRequestStatus.PENDING, "사유2",
                    LocalDateTime.of(2025, 7, 5, 10, 0));

            saveVacation(user2, LocalDate.of(2025, 6, 10), 2, annual, VacationRequestStatus.REJECTED, "사유3",
                    LocalDateTime.of(2025, 6, 10, 15, 0));
        }

        private void saveVacation(UserEntity user, LocalDate startDate, int days, VacationTypeEntity type,
                                  VacationRequestStatus status, String reason, LocalDateTime createdAt) {
            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .user(user)
                    .vacationType(type)
                    .startDate(startDate)
                    .endDate(startDate.plusDays(days - 1))
                    .daysCount(BigDecimal.valueOf(days))
                    .status(status)
                    .reason(reason)
                    .createdAt(createdAt)
                    .build());
        }

        @Test
        @DisplayName("전체 조건 적용 (ALL)")
        void testFindAllWithoutFilters() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("ALL")
                    .name(null)
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("유형 필터(ANNUAL) 적용")
        void testTypeCodeFilter() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ANNUAL")
                    .status("ALL")
                    .name(null)
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result.getTotalElements()).isEqualTo(2); // user1 + user2 각각 연차 1건
            assertThat(result.getContent())
                    .extracting("typeCode")
                    .containsOnly("ANNUAL");
        }

        @Test
        @DisplayName("상태 필터(APPROVED) 적용")
        void testStatusFilter() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("APPROVED")
                    .name(null)
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1); // user1의 연차 1건 (APPROVED)
            assertThat(result.getContent())
                    .extracting("status")
                    .containsOnly("APPROVED");
        }

        @Test
        @DisplayName("이름 필터('선') 적용")
        void testNameContainsFilter() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("ALL")
                    .name("선")
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result.getTotalElements()).isEqualTo(2); // user1이 만든 2건
            assertThat(result.getContent())
                    .extracting("name", String.class)
                    .allMatch(name -> name.contains("선"));
        }

        @Test
        @DisplayName("연도/월 필터(2025년 7월) 적용")
        void testYearMonthFilter() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("ALL")
                    .name(null)
                    .year("2025")
                    .month("7")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result.getTotalElements()).isEqualTo(2); // user1의 7월 신청 2건
            assertThat(result.getContent())
                    .extracting("createdAt", LocalDateTime.class)
                    .allMatch(dt -> dt.getYear() == 2025 && dt.getMonthValue() == 7);
        }

        @Test
        @DisplayName("혼합 필터(유형+상태+이름+연월) 적용")
        void testMixedFilters() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ANNUAL")
                    .status("APPROVED")
                    .name("선")
                    .year("2025")
                    .month("7")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            // 예상: user1이 만든 ANNUAL + APPROVED + 이름에 '선' 포함 + 2025년 7월 신청 건만 필터링되어야 함
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);

            assertThat(result.getContent())
                    .extracting("typeCode")
                    .containsOnly("ANNUAL");

            assertThat(result.getContent())
                    .extracting("status")
                    .containsOnly("APPROVED");

            assertThat(result.getContent())
                    .extracting("name", String.class)
                    .allMatch(name -> name.contains("선"));

            assertThat(result.getContent())
                    .extracting("createdAt", LocalDateTime.class)
                    .allMatch(dt -> dt.getYear() == 2025 && dt.getMonthValue() == 7);
        }

        @Test
        @DisplayName("정렬 조건(asc) 적용 - createdAt 오름차순으로 정렬")
        void testAscendingOrder() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("ALL")
                    .name(null)
                    .year("ALL")
                    .month("ALL")
                    .order("asc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSizeGreaterThan(1);      // 정렬 비교를 위해 충분한 데이터(2개 이상)가 있는지 확인

            for (int i = 0; i < result.getContent().size() - 1; i++) {
                LocalDateTime current = result.getContent().get(i).getCreatedAt();
                LocalDateTime next = result.getContent().get(i + 1).getCreatedAt();

                assertThat(current).isBeforeOrEqualTo(next);
            }
        }

        @Test
        @DisplayName("정렬 조건(desc) 적용 - createdAt 내림차순으로 정렬")
        void testDescendingOrder() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("ALL")
                    .name(null)
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSizeGreaterThan(1);

            for (int i = 0; i < result.getContent().size() - 1; i++) {
                LocalDateTime current = result.getContent().get(i).getCreatedAt();
                LocalDateTime next = result.getContent().get(i + 1).getCreatedAt();

                assertThat(current).isAfterOrEqualTo(next);
            }
        }

        @Test
        @DisplayName("페이징 처리 (page=0, size=2) - 첫 페이지 조회")
        void testFirstPage() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("ALL")
                    .name(null)
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(0, 2);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(result.getContent().size());   // 전체 데이터 수가 현재 페이지 데이터 수보다 같거나 커야 함
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(2);        //  조회된 결과가 최대 2개까지만 반환되는지 확인
            assertThat(result.getContent()).hasSize(2);


        }

        @Test
        @DisplayName("페이징 처리 (page=1, size=2) - 두 번째 페이지 조회")
        void testSecondPage() {
            AdminVacationRequestSearchCond cond = AdminVacationRequestSearchCond.builder()
                    .typeCode("ALL")
                    .status("ALL")
                    .name(null)
                    .year("ALL")
                    .month("ALL")
                    .order("desc")
                    .build();

            Pageable pageable = PageRequest.of(1, 2);

            Page<AdminVacationRequestListResponse> result =
                    vacationRequestRepositoryImpl.findAllVacationRequestsForAdmin(cond, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(result.getContent().size());
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(2);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findApprovedVacationsForCalendar 쿼리 테스트")
    class findApprovedVacationsForCalendarTest {
        private UserEntity user;
        private VacationTypeEntity annualVacationType;

        @BeforeEach
        void setUp() {
            user = userRepository.save(UserEntity.builder()
                    .username("user1")
                    .name("선우")
                    .status(UserStatus.ACTIVE)
                    .build());

            annualVacationType = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .typeName("연차")
                    .build());
        }

        @Test
        @DisplayName("완전히 범위 내에 휴가가 속한 경우")
        void testVacationCompletelyWithinRange() {
            // given
            LocalDate rangeStart = LocalDate.of(2025, 7, 1);
            LocalDate rangeEnd = LocalDate.of(2025, 7, 31);

            // 휴가 기간 : 범위 내 완전히 속함 (2025-07-10 ~ 2025-07-12)
            VacationRequestEntity vacation = createVacation(LocalDate.of(2025, 7, 10), LocalDate.of(2025, 7, 12), BigDecimal.valueOf(3));

            // when
            List<VacationCalendarResponse> results = vacationRequestRepository.findApprovedVacationsForCalendar(user.getUserId(), rangeStart, rangeEnd);

            // then
            assertThat(results).hasSize(1);
            assertThat(results)
                    .extracting("requestId")
                    .contains(vacation.getRequestId());
        }

        @Test
        @DisplayName("휴가가 범위를 일부만 겹치는 경우 (앞부분)")
        void testVacationOverlappingRangeAtStart() {
            // given
            LocalDate rangeStart = LocalDate.of(2025, 7, 1);
            LocalDate rangeEnd = LocalDate.of(2025, 7, 31);

            // 휴가 기간: 범위 앞부분과 겹침 (2025-06-29 ~ 2025-07-02)
            VacationRequestEntity vacation = createVacation(LocalDate.of(2025, 6, 29), LocalDate.of(2025, 7, 2), BigDecimal.valueOf(3));

            // when
            List<VacationCalendarResponse> results = vacationRequestRepository.findApprovedVacationsForCalendar(user.getUserId(), rangeStart, rangeEnd);

            // then
            assertThat(results).hasSize(1);
            assertThat(results)
                    .extracting("requestId")
                    .containsExactly(vacation.getRequestId());
        }

        @Test
        @DisplayName("휴가가 범위를 일부만 겹치는 경우 (뒷부분)")
        void testVacationOverlappingRangeAtEnd() {
            // given
            LocalDate rangeStart = LocalDate.of(2025, 7, 1);
            LocalDate rangeEnd = LocalDate.of(2025, 7, 31);

            // 휴가 기간: 범위 뒷부분과 겹침 (2025-07-30 ~ 2025-08-02)
            VacationRequestEntity vacation = createVacation(LocalDate.of(2025, 7, 30), LocalDate.of(2025, 8, 2), BigDecimal.valueOf(4));

            // when
            List<VacationCalendarResponse> results = vacationRequestRepository.findApprovedVacationsForCalendar(user.getUserId(), rangeStart, rangeEnd);

            // then
            assertThat(results).hasSize(1);
            assertThat(results)
                    .extracting("requestId")
                    .containsExactly(vacation.getRequestId());
        }

        @Test
        @DisplayName("휴가가 범위 밖에 있는 경우 (시작·종료일 모두 이전)")
        void testVacationBeforeRange() {
            // given
            LocalDate rangeStart = LocalDate.of(2025, 7, 1);
            LocalDate rangeEnd = LocalDate.of(2025, 7, 31);

            // 휴가 기간: 범위 이전 (2025-06-01 ~ 2025-06-15)
            VacationRequestEntity vacation = createVacation(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 15), BigDecimal.valueOf(11));

            // when
            List<VacationCalendarResponse> results = vacationRequestRepository.findApprovedVacationsForCalendar(user.getUserId(), rangeStart, rangeEnd);

            // then
            // 이 휴가는 범위 밖이므로 결과에 포함되지 않아야 함
            assertThat(results)
                    .extracting("requestId")
                    .doesNotContain(vacation.getRequestId());
        }

        @Test
        @DisplayName("휴가가 범위 밖에 있는 경우 (시작·종료일 모두 이후)")
        void testVacationAfterRange() {
            // given
            LocalDate rangeStart = LocalDate.of(2025, 7, 1);
            LocalDate rangeEnd = LocalDate.of(2025, 7, 31);

            // 휴가 기간: 범위 이후 (2025-08-01 ~ 2025-08-05)
            VacationRequestEntity vacation = createVacation(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5), BigDecimal.valueOf(5));

            // when
            List<VacationCalendarResponse> results = vacationRequestRepository.findApprovedVacationsForCalendar(user.getUserId(), rangeStart, rangeEnd);

            // then
            // 범위 밖이므로 결과에서 제외되어야 함
            assertThat(results)
                    .extracting("requestId")
                    .doesNotContain(vacation.getRequestId());
        }

        @Test
        @DisplayName("휴가 상태가 APPROVED가 아닌 경우")
        void testVacationStatusNotApproved() {
            // given
            LocalDate rangeStart = LocalDate.of(2025, 7, 1);
            LocalDate rangeEnd = LocalDate.of(2025, 7, 31);

            // 휴가 엔티티를 직접 생성해서 status를 PENDING 으로 설정
            VacationRequestEntity vacation = VacationRequestEntity.builder()
                    .user(user)
                    .vacationType(annualVacationType)
                    .startDate(LocalDate.of(2025, 7, 10))
                    .endDate(LocalDate.of(2025, 7, 12))
                    .daysCount(BigDecimal.valueOf(3))
                    .status(VacationRequestStatus.PENDING)  // APPROVED 아님
                    .build();
            vacationRequestRepository.save(vacation);

            // when
            List<VacationCalendarResponse> results = vacationRequestRepository.findApprovedVacationsForCalendar(user.getUserId(), rangeStart, rangeEnd);

            // then
            // 상태가 APPROVED가 아니므로 결과에 포함 안 됨
            assertThat(results)
                    .extracting("requestId")
                    .doesNotContain(vacation.getRequestId());
        }

        private VacationRequestEntity createVacation(LocalDate start, LocalDate end, BigDecimal daysCount) {
            VacationRequestEntity vacation = VacationRequestEntity.builder()
                    .user(user)
                    .vacationType(annualVacationType)
                    .startDate(start)
                    .endDate(end)
                    .daysCount(daysCount)
                    .status(VacationRequestStatus.APPROVED)
                    .build();
            return vacationRequestRepository.save(vacation);
        }
    }

    @Nested
    @DisplayName("findUsageStatisticsByYear 쿼리 테스트")
    class findUsageStatisticsByYearTest {

        @BeforeEach
        void setUp() {
            VacationTypeEntity annualType = vacationTypeRepository.save(
                    VacationTypeEntity.builder()
                            .typeCode("ANNUAL")
                            .typeName("연차")
                            .build()
            );

            VacationTypeEntity sickType = vacationTypeRepository.save(
                    VacationTypeEntity.builder()
                            .typeCode("SICK")
                            .typeName("병가")
                            .build()
            );

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .vacationType(annualType)
                    .startDate(LocalDate.of(2025, 1, 10))
                    .daysCount(new BigDecimal("1"))
                    .status(VacationRequestStatus.APPROVED)
                    .build());

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .vacationType(annualType)
                    .startDate(LocalDate.of(2025, 1, 20))
                    .daysCount(new BigDecimal("2"))
                    .status(VacationRequestStatus.APPROVED)
                    .build());

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .vacationType(sickType)
                    .startDate(LocalDate.of(2025, 2, 5))
                    .daysCount(new BigDecimal("1.5"))
                    .status(VacationRequestStatus.APPROVED)
                    .build());

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .vacationType(annualType)
                    .startDate(LocalDate.of(2025, 3, 10))
                    .daysCount(new BigDecimal("4"))
                    .status(VacationRequestStatus.REJECTED)
                    .build());

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .vacationType(annualType)
                    .startDate(LocalDate.of(2024, 12, 25))
                    .daysCount(new BigDecimal("1"))
                    .status(VacationRequestStatus.APPROVED)
                    .build());
        }

        @Test
        @DisplayName("2025년 월별 휴가 사용 통계 조회")
        void testFindUsageStatisticsByYear() {
            List<VacationUsageStatisticsRaw> results = vacationRequestRepository.findUsageStatisticsByYear(2025);

            assertThat(results).hasSize(2);  // 예: 1월 연차 + 2월 병가만 있다고 가정

            VacationUsageStatisticsRaw janAnnual = results.stream()
                    .filter(r -> r.getTypeCode().equals("ANNUAL") && r.getMonth() == 1)
                    .findFirst().orElseThrow();

            assertThat(janAnnual.getTotalUsedDays()).isEqualByComparingTo(new BigDecimal("3"));

            VacationUsageStatisticsRaw febSick = results.stream()
                    .filter(r -> r.getTypeCode().equals("SICK") && r.getMonth() == 2)
                    .findFirst().orElseThrow();

            assertThat(febSick.getTotalUsedDays()).isEqualByComparingTo(new BigDecimal("1.5"));
        }
    }

    @Nested
    @DisplayName("findMonthlyStatusChangeCounts 쿼리 테스트")
    class findMonthlyStatusChangeCounts {

        @BeforeEach
        void setUp() {
            UserEntity user = userRepository.save(UserEntity.builder()
                    .username("user1")
                    .name("선우")
                    .status(UserStatus.ACTIVE)
                    .build());

            VacationTypeEntity annualType = vacationTypeRepository.save(VacationTypeEntity.builder()
                    .typeCode("ANNUAL")
                    .typeName("연차")
                    .build());

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .user(user)
                    .vacationType(annualType)
                    .startDate(LocalDate.of(2025, 8, 15))
                    .status(VacationRequestStatus.APPROVED)
                    .daysCount(new BigDecimal("2"))
                    .build());

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .user(user)
                    .vacationType(annualType)
                    .startDate(LocalDate.of(2025, 8, 20))
                    .status(VacationRequestStatus.ON_HOLD)
                    .daysCount(new BigDecimal("1"))
                    .build());

            vacationRequestRepository.save(VacationRequestEntity.builder()
                    .user(user)
                    .vacationType(annualType)
                    .startDate(LocalDate.of(2025, 7, 10))
                    .status(VacationRequestStatus.REJECTED)
                    .daysCount(new BigDecimal("3"))
                    .build());
        }

        @Test
        @DisplayName("2025년 8월 상태별 휴가 통계 조회")
        void testFindMonthlyStatusChangeCounts() {
            List<VacationStatusChangeStatisticsResponse> results =
                    vacationRequestRepositoryImpl.findMonthlyStatusChangeCounts(2025, 8);

            // 상태별 건수 확인 (APPROVED, PENDING 두 개 있어야 함)
            assertThat(results).hasSize(2);

            // APPROVED 건수 1
            assertThat(results).anyMatch(r -> r.getNewStatus().equals("APPROVED") && r.getTotalCount() == 1L);

            // PENDING 건수 1
            assertThat(results).anyMatch(r -> r.getNewStatus().equals("ON_HOLD") && r.getTotalCount() == 1L);

            // REJECTED 상태는 7월에 있으므로 포함 안 됨
            assertThat(results).noneMatch(r -> r.getNewStatus().equals("REJECTED"));
        }
    }
}
