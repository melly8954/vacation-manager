package com.melly.vacationmanager.domain.user.repository;

import com.melly.vacationmanager.domain.user.entity.QUserEntity;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserEntity> findPendingUsers(String name, Integer year, Integer month, Pageable pageable) {
        QUserEntity q = QUserEntity.userEntity;
        // 동적 조건을 누적할 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();

        // 상태가 PENDING인 사용자만 필터링
        builder.and(q.status.eq(UserStatus.PENDING));

        // name이 null 아니고 빈 문자열이 아니면, 이름에 해당 문자열 포함하는 조건 추가 (대소문자 무시)
        if (name != null && !name.isEmpty()) {
            builder.and(q.name.containsIgnoreCase(name));
        }

        // year, month가 모두 있을 경우
        if (year != null && month != null) {
            LocalDateTime startDate = LocalDate.of(year, month, 1).atStartOfDay();      // 해당 연도, 월의 시작일 00:00:00 시각 생성
            LocalDateTime endDate = startDate.withDayOfMonth(startDate.toLocalDate().lengthOfMonth())       // 해당 월의 마지막 일 23:59:59.999999999 시각 생성
                    .with(LocalTime.MAX);

            builder.and(q.createdAt.between(startDate, endDate));       // createdAt 컬럼이 startDate와 endDate 사이인 조건 추가
        } else {
            if (year != null) {     // year만 있으면 year 기준 조건 추가
                builder.and(q.createdAt.year().eq(year));
            }
            if (month != null) {   // month만 있으면 month 기준 조건 추가
                builder.and(q.createdAt.month().eq(month));
            }
        }

        // Pageable 객체에서 정렬 기준을 추출하는 유틸 함수 호출 (아래 getOrderSpecifier 참고)
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(pageable);

        // 조건에 맞는 사용자 목록을 조회 (페이징 및 정렬 포함)
        List<UserEntity> content = queryFactory
                .selectFrom(q)
                .where(builder)
                .offset(pageable.getOffset())          // 페이지 시작 위치 설정 (0-based index)
                .limit(pageable.getPageSize())        // 페이지 크기만큼 조회 제한
                .orderBy(orderSpecifier)               // 정렬 조건 적용
                .fetch();                             // 실제 쿼리 실행 및 결과 반환

        // 전체 조건에 맞는 사용자 수를 조회
        Long total = queryFactory
                .select(q.count())
                .from(q)
                .where(builder)
                .fetchOne();

        // 조회된 총 건수가 null일 경우 0으로 초기화
        if (total == null) {
            total = 0L;
        }

        // 조회된 내용과 페이징 정보, 전체 개수를 포함하는 Page 객체 반환
        return new PageImpl<>(content, pageable, total);
    }

    // 정렬 동적 처리
    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        Sort sort = pageable.getSort();

        for (Sort.Order order : sort) {
            PathBuilder<UserEntity> pathBuilder = new PathBuilder<>(UserEntity.class, "userEntity");
            return new OrderSpecifier<>(
                    order.getDirection().isAscending() ? Order.ASC : Order.DESC,
                    pathBuilder.getComparable(order.getProperty(), Comparable.class)
            );
        }

        // 기본 정렬 (없을 경우)
        return QUserEntity.userEntity.userId.desc();
    }
}
