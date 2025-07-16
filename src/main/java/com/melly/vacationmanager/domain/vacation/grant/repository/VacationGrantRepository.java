package com.melly.vacationmanager.domain.vacation.grant.repository;

import com.melly.vacationmanager.domain.vacation.grant.entity.VacationGrantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacationGrantRepository extends JpaRepository<VacationGrantEntity, Long> {
}
