package com.melly.vacationmanager.domain.vacation.auditlog.repository;

import com.melly.vacationmanager.domain.admin.vacation.statistic.dto.VacationStatusChangeStatisticsResponse;
import com.melly.vacationmanager.domain.vacation.auditlog.entity.QVacationAuditLogEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class VacationAuditLogRepositoryImpl implements VacationAuditLogRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<VacationStatusChangeStatisticsResponse> findMonthlyStatusChangeCounts(int year, int month) {
        QVacationAuditLogEntity q = QVacationAuditLogEntity.vacationAuditLogEntity;

        return queryFactory
                .select(Projections.constructor(
                        VacationStatusChangeStatisticsResponse.class,
                        q.newStatus,
                        q.newStatus.count()
                ))
                .from(q)
                .where(
                        q.oldStatus.eq("PENDING"),
                        q.newStatus.in("APPROVED", "REJECTED", "ON_HOLD"),
                        q.changeDate.year().eq(year),
                        q.changeDate.month().eq(month)
                )
                .groupBy(q.newStatus)
                .fetch();
    }
}
