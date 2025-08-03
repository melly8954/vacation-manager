package com.melly.vacationmanager.domain.vacation.grant.repository;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationGrantStatisticsResponse;
import com.melly.vacationmanager.domain.vacation.grant.entity.QVacationGrantEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class VacationGrantRepositoryImpl implements VacationGrantRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<VacationGrantStatisticsResponse> findGrantStatisticsBetween(LocalDate start, LocalDate end) {
        QVacationGrantEntity g = QVacationGrantEntity.vacationGrantEntity;

        return queryFactory
                .select(Projections.constructor(
                        VacationGrantStatisticsResponse.class,
                        g.type.typeCode,
                        g.type.typeName,
                        Expressions.numberTemplate(BigDecimal.class, "coalesce(sum({0}), 0)", g.grantedDays)
                ))
                .from(g)
                .where(g.grantDate.between(start, end))
                .groupBy(g.type.typeCode, g.type.typeName)
                .fetch();
    }
}
