package com.melly.vacationmanager.domain.user.repository;

import com.melly.vacationmanager.config.QueryDslTestConfig;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslTestConfig.class)
@ActiveProfiles("test") // application-test.yml 설정을 적용
class UserRepositoryImplTest {
    @Autowired
    private UserRepository userRepository; // JpaRepository + Custom

    @Autowired
    private EntityManager em;

    // 공통으로 쓰일 테스트 데이터 삽입 헬퍼 메서드
    private void createTestUser(String name, UserStatus status, LocalDateTime createdAt) {
        UserEntity user = UserEntity.builder()
                .name(name)
                .status(status)
                .createdAt(createdAt)
                .build();
        em.persist(user);
    }

    @BeforeEach
    void setup() {
        // 여러 사용자 데이터 생성해서 상황별 테스트에 활용
        LocalDateTime now = LocalDateTime.of(2025, 7, 10, 0, 0);

        createTestUser("인증된유저1", UserStatus.ACTIVE, now);
        createTestUser("대기유저1", UserStatus.PENDING, now.minusMonths(1));
        createTestUser("대기유저2", UserStatus.PENDING, now.minusDays(1));
        createTestUser("대기유저3", UserStatus.PENDING, now);
        createTestUser("대기인원1", UserStatus.PENDING, now);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("기본 정상 흐름 - 모든 필터 null, 대기중 사용자 전체 조회")
    void testFindPendingUsers_noFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> result = userRepository.findPendingUsers(null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(4); // PENDING 상태 4명
    }

    @Test
    @DisplayName("이름 검색 필터 - '유저' 검색")
    void testFindPendingUsers_nameFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> result = userRepository.findPendingUsers("유저", null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(user -> user.getName().contains("유저"));
    }

    @Test
    @DisplayName("이름 필터 빈 문자열 또는 null - 필터 무시")
    void testFindPendingUsers_nameFilter_emptyOrNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> resultEmpty = userRepository.findPendingUsers("", null, null, pageable);
        Page<UserEntity> resultNull = userRepository.findPendingUsers(null, null, null, pageable);

        assertThat(resultEmpty.getTotalElements()).isEqualTo(4);
        assertThat(resultNull.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("연도 필터 - 2025년")
    void testFindPendingUsers_yearFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> result = userRepository.findPendingUsers(null, 2025, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("월 필터 - 7월")
    void testFindPendingUsers_monthFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> result = userRepository.findPendingUsers(null, null, 7, pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("연도+월 필터 조합 - 2025년 7월")
    void testFindPendingUsers_yearMonthFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> result = userRepository.findPendingUsers(null, 2025, 7, pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("이름+연도+월 필터 조합")
    void testFindPendingUsers_allFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> result = userRepository.findPendingUsers("유저", 2025, 7, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(user -> user.getName().contains("유저"));
    }

    @Test
    @DisplayName("조건에 맞는 데이터 없을 때 빈 리스트 반환")
    void testFindPendingUsers_noMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> result = userRepository.findPendingUsers("없는이름", 2020, 1, pageable);

        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("페이지 처리 검증")
    void testFindPendingUsers_paging() {
        Pageable pageable = PageRequest.of(0, 2); // 한 페이지에 2개씩
        Page<UserEntity> firstPage = userRepository.findPendingUsers(null, null, null, pageable);
        assertThat(firstPage.getContent()).hasSize(2);

        Pageable secondPageable = PageRequest.of(1, 2);
        Page<UserEntity> secondPage = userRepository.findPendingUsers(null, null, null, secondPageable);
        assertThat(secondPage.getContent()).hasSize(2);
    }
}