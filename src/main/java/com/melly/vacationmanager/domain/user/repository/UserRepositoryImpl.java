package com.melly.vacationmanager.domain.user.repository;

import com.melly.vacationmanager.domain.user.entity.QUserEntity;
import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

        List<UserEntity> content = queryFactory
                .selectFrom(user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.userId.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .where(builder)
                .fetch()
                .size(); // fetchCount() 대신

        return new PageImpl<>(content, pageable, total);
    }
}
