package com.melly.vacationmanager.domain.vacation.request.repository;

import com.melly.vacationmanager.domain.vacation.request.entity.QVacationRequestEntity;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

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
}
