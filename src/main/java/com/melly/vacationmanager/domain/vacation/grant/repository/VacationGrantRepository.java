package com.melly.vacationmanager.domain.vacation.grant.repository;

import com.melly.vacationmanager.domain.vacation.grant.entity.VacationGrantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface VacationGrantRepository extends JpaRepository<VacationGrantEntity, Long> {
    @Query("SELECT COALESCE(SUM(g.grantedDays), 0) " +
            "FROM VacationGrantEntity g " +
            "WHERE g.user.userId = :userId AND g.type.typeCode = :typeCode")
    BigDecimal sumGrantedDays(@Param("userId") Long userId, @Param("typeCode") String typeCode);
}
