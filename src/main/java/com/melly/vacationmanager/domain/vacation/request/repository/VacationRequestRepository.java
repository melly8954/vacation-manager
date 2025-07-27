package com.melly.vacationmanager.domain.vacation.request.repository;

import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequestEntity, Long>, VacationRequestRepositoryCustom {

    boolean existsByRequestId(Long vacationRequestId);

    Optional<VacationRequestEntity> findByRequestId(Long requestId);

}
