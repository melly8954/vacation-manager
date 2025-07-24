package com.melly.vacationmanager.domain.vacation.request.repository;

import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestListResponse;
import com.melly.vacationmanager.domain.vacation.request.entity.QVacationRequestEntity;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VacationRequestRepositoryImpl implements VacationRequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsApprovedOverlap(Long userId, LocalDate startDate, LocalDate endDate) {
        QVacationRequestEntity vacation = QVacationRequestEntity.vacationRequestEntity;

        return queryFactory
                .selectOne()
                .from(vacation)
                .where(
                        vacation.user.userId.eq(userId),
                        vacation.status.eq(VacationRequestStatus.APPROVED),
                        vacation.startDate.loe(endDate),
                        vacation.endDate.goe(startDate)
                )
                .fetchFirst() != null;
    }

    @Override
    public Page<VacationRequestListResponse> findMyVacationRequests(VacationRequestSearchCond cond, Pageable pageable) {
        QVacationRequestEntity q = QVacationRequestEntity.vacationRequestEntity;

        BooleanBuilder builder = new BooleanBuilder()
                .and(q.user.userId.eq(cond.getUserId()))
                .and(!"ALL".equals(cond.getTypeCode()) ? q.vacationType.typeCode.eq(cond.getTypeCode()) : null)
                .and(!"ALL".equals(cond.getStatus()) ? q.status.eq(VacationRequestStatus.valueOf(cond.getStatus())) : null);

        if (!"ALL".equals(cond.getYear())) {
            builder.and(q.startDate.year().eq(Integer.parseInt(cond.getYear())));
        }

        if (!"ALL".equals(cond.getMonth())) {
            builder.and(q.startDate.month().eq(Integer.parseInt(cond.getMonth())));
        }

        List<VacationRequestListResponse> content = queryFactory
                .select(Projections.constructor(
                        VacationRequestListResponse.class,
                        q.requestId,
                        q.startDate,
                        q.endDate,
                        q.vacationType.typeCode,
                        q.daysCount,
                        q.status,
                        q.reason,
                        q.createdAt
                ))
                .from(q)
                .where(builder)
                .orderBy("asc".equalsIgnoreCase(cond.getOrder()) ? q.createdAt.asc() : q.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                queryFactory
                        .select(q.count())
                        .from(q)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
