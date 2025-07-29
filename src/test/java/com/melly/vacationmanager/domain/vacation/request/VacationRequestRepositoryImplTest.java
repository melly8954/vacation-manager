package com.melly.vacationmanager.domain.vacation.request;

import com.melly.vacationmanager.config.QueryDslTestConfig;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestListResponse;
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
    @DisplayName("findMyVacationRequests 조건 필터 테스트")
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
        @DisplayName("휴가기간 기준 (vacationPeriod) 필터 - 2025년 6월")
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
        @DisplayName("휴가기간 기준 (vacationPeriod) - 현재 연도 7월")
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
    @DisplayName("findAllVacationRequestsForAdmin 조건 필터 테스트")
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
}
