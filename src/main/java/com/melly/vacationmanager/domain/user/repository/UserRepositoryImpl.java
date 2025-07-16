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
        QUserEntity user = QUserEntity.userEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(user.status.eq(UserStatus.PENDING));
        if (name != null && !name.isEmpty()) builder.and(user.name.containsIgnoreCase(name));

        if (year != null && month != null) {
            LocalDateTime startDate = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime endDate = startDate.withDayOfMonth(startDate.toLocalDate().lengthOfMonth())
                    .with(LocalTime.MAX);
            builder.and(user.createdAt.between(startDate, endDate));
        } else {
            if (year != null)
                builder.and(user.createdAt.year().eq(year));
            if (month != null)
                builder.and(user.createdAt.month().eq(month));
        }

        // 정렬 처리: Pageable에서 Sort 정보 추출
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(pageable);

        List<UserEntity> content = queryFactory
                .selectFrom(user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifier)
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .where(builder)
                .fetch()
                .size(); // fetchCount() 대신

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
