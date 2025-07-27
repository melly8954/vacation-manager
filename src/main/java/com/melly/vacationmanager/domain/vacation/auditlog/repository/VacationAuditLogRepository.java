package com.melly.vacationmanager.domain.vacation.auditlog.repository;

import com.melly.vacationmanager.domain.vacation.auditlog.entity.VacationAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacationAuditLogRepository extends JpaRepository<VacationAuditLogEntity, Long> {
}
