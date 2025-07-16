package com.melly.vacationmanager.domain.vacation.type.repository;

import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VacationTypeRepository extends JpaRepository<VacationTypeEntity, String> {
    Optional<VacationTypeEntity> findByTypeCode(String typeCode);
}
