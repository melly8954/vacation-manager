package com.melly.vacationmanager.domain.vacation.request.repository;

import com.melly.vacationmanager.domain.admin.vacation.request.dto.request.AdminVacationRequestSearchCond;
import com.melly.vacationmanager.domain.admin.vacation.request.dto.response.AdminVacationRequestListResponse;
import com.melly.vacationmanager.domain.user.entity.QUserEntity;
import com.melly.vacationmanager.domain.vacation.request.dto.request.VacationRequestSearchCond;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationCalendarResponse;
import com.melly.vacationmanager.domain.vacation.request.dto.response.VacationRequestListResponse;
import com.melly.vacationmanager.domain.vacation.request.entity.QVacationRequestEntity;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
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
        QVacationRequestEntity q = QVacationRequestEntity.vacationRequestEntity;

        return queryFactory
                .selectOne()
                .from(q)
                .where(
                        q.user.userId.eq(userId),
                        q.status.eq(VacationRequestStatus.APPROVED),
                        q.startDate.loe(endDate),
                        q.endDate.goe(startDate)
                )
                .fetchFirst() != null;
    }

    @Override
    public Page<VacationRequestListResponse> findMyVacationRequests(VacationRequestSearchCond cond, Pageable pageable) {
        QVacationRequestEntity q = QVacationRequestEntity.vacationRequestEntity;
        // 동적 조건을 누적할 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();

        // 사용자 ID는 무조건 필터링
        builder.and(q.user.userId.eq(cond.getUserId()));

        // typeCode "ALL"이 아닐 때만 조건 추가
        if (!"ALL".equals(cond.getTypeCode())) {
            builder.and(q.vacationType.typeCode.eq(cond.getTypeCode()));
        }

        // status "ALL"이 아닐 때만 조건 추가
        if (!"ALL".equals(cond.getStatus())) {
            builder.and(q.status.eq(VacationRequestStatus.valueOf(cond.getStatus())));
        }

        // 연도/월 필터
        if ("vacationPeriod".equals(cond.getDateFilterType())) {
            // startDate ~ endDate 기준 기간 겹침 조건 적용
            // year, month 둘 다 지정된 경우
            if (!"ALL".equals(cond.getYear()) && !"ALL".equals(cond.getMonth())) {
                int year = Integer.parseInt(cond.getYear());
                int month = Integer.parseInt(cond.getMonth());

                // 필터 기간: 해당 연도의 해당 월 1일 ~ 말일
                LocalDate filterStart = LocalDate.of(year, month, 1);
                LocalDate filterEnd = filterStart.withDayOfMonth(filterStart.lengthOfMonth());

                // 휴가 기간이 필터 기간과 겹치는 경우만 조회
                builder.and(q.endDate.goe(filterStart)
                        .and(q.startDate.loe(filterEnd)));

            } else if (!"ALL".equals(cond.getYear())) {     // year만 지정된 경우
                int year = Integer.parseInt(cond.getYear());

                // 필터 기간: 해당 연도의 1월 1일 ~ 12월 31일
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);

                // 휴가 기간이 필터 기간과 겹치는 경우만 조회
                builder.and(q.endDate.goe(yearStart)
                        .and(q.startDate.loe(yearEnd)));

            } else if (!"ALL".equals(cond.getMonth())) {    // month만 지정된 경우 (year는 ALL)
                int month = Integer.parseInt(cond.getMonth());
                int currentYear = LocalDate.now().getYear(); // 현재 연도 기준 필터링

                // 필터 기간: 현재 연도의 해당 월 1일 ~ 말일
                LocalDate monthStart = LocalDate.of(currentYear, month, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

                // 휴가 기간이 필터 기간과 겹치는 경우만 조회
                builder.and(q.endDate.goe(monthStart)
                        .and(q.startDate.loe(monthEnd)));
            }
        } else {
            // 기본 createdAt 기준 필터링
            if (!"ALL".equals(cond.getYear())) {
                builder.and(q.createdAt.year().eq(Integer.parseInt(cond.getYear())));
            }
            if (!"ALL".equals(cond.getMonth())) {
                builder.and(q.createdAt.month().eq(Integer.parseInt(cond.getMonth())));
            }
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

    @Override
    public Page<AdminVacationRequestListResponse> findAllVacationRequestsForAdmin(AdminVacationRequestSearchCond cond, Pageable pageable) {
        QVacationRequestEntity request = QVacationRequestEntity.vacationRequestEntity;
        QUserEntity user = QUserEntity.userEntity;

        BooleanBuilder builder = new BooleanBuilder();

        // 휴가 유형 필터 조건 추가 (ALL 이면 조건 추가하지 않음)
        if (cond.getTypeCode() != null && !"ALL".equalsIgnoreCase(cond.getTypeCode())) {
            builder.and(request.vacationType.typeCode.eq(cond.getTypeCode()));
        }

        // 휴가 상태 필터 조건 추가 (ALL 이면 조건 추가하지 않음)
        if (cond.getStatus() != null && !"ALL".equalsIgnoreCase(cond.getStatus())) {
            builder.and(request.status.eq(VacationRequestStatus.valueOf(cond.getStatus())));
        }

        // 사용자 이름 포함 검색 조건 추가 (이름이 빈 문자열 또는 null 아닐 경우)
        if (cond.getName() != null && !cond.getName().isBlank()) {
            builder.and(user.name.containsIgnoreCase(cond.getName()));
        }

        // 신청일(createdAt) 연도/월 필터링 (LocalDateTime)
        if (cond.getYear() != null && !"ALL".equalsIgnoreCase(cond.getYear())) {
            builder.and(request.createdAt.year().eq(Integer.parseInt(cond.getYear())));
        }

        if (cond.getMonth() != null && !"ALL".equalsIgnoreCase(cond.getMonth())) {
            builder.and(request.createdAt.month().eq(Integer.parseInt(cond.getMonth())));
        }

        OrderSpecifier<?> orderSpecifier = "asc".equalsIgnoreCase(cond.getOrder())
                ? request.createdAt.asc()
                : request.createdAt.desc();

        List<AdminVacationRequestListResponse> content = queryFactory
                .select(Projections.constructor(AdminVacationRequestListResponse.class,
                        request.requestId,
                        user.userId,
                        user.name,
                        user.username,
                        request.vacationType.typeCode,
                        request.status.stringValue(), // 또는 name()
                        request.startDate,
                        request.endDate,
                        request.daysCount,
                        request.reason,
                        request.createdAt
                ))
                .from(request)
                .join(request.user, user)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                queryFactory
                        .select(request.count())
                        .from(request)
                        .join(request.user, user)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<VacationCalendarResponse> findApprovedVacationsForCalendar(Long userId, LocalDate start, LocalDate end) {
        QVacationRequestEntity q = QVacationRequestEntity.vacationRequestEntity;

        return queryFactory
                .select(Projections.constructor(
                        VacationCalendarResponse.class,
                        q.requestId,
                        q.vacationType.typeCode,
                        q.vacationType.typeName,
                        q.startDate,
                        q.endDate,
                        q.daysCount,
                        q.status.stringValue()
                ))
                .from(q)
                .where(
                        q.user.userId.eq(userId),
                        q.status.eq(VacationRequestStatus.APPROVED),
                        q.endDate.goe(start),
                        q.startDate.loe(end)
                )
                .orderBy(q.startDate.asc())
                .fetch();
    }
}
