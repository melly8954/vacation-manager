package com.melly.vacationmanager.domain.vacation.balance.repository;

import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacationBalanceRepository extends JpaRepository<VacationBalanceEntity, VacationBalanceId > {
    // 불필요한 추가 쿼리 방지 (N+1 방지) 및 성능 최적화
    @EntityGraph(attributePaths = {"type"})
    List<VacationBalanceEntity> findByUser_UserId(Long userId);
}
