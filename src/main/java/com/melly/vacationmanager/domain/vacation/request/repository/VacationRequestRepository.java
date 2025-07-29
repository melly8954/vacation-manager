package com.melly.vacationmanager.domain.vacation.request.repository;

import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequestEntity, Long>, VacationRequestRepositoryCustom {

    boolean existsByRequestId(Long vacationRequestId);

    Optional<VacationRequestEntity> findByRequestId(Long requestId);

    @Query("SELECT COALESCE(SUM(r.daysCount), 0) " +
            "FROM VacationRequestEntity r " +
            "WHERE r.user.userId = :userId " +
            "AND r.vacationType.typeCode = :typeCode " +
            "AND r.status = :status")
    BigDecimal sumUsedDays(@Param("userId") Long userId,
                           @Param("typeCode") String typeCode,
                           @Param("status") VacationRequestStatus status);
}
