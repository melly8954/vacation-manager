package com.melly.vacationmanager.domain.vacation.balance.repository;

import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VacationBalanceRepository extends JpaRepository<VacationBalanceEntity, VacationBalanceId > {
}
