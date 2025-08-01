package com.melly.vacationmanager.domain.vacation.grant.service;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.user.repository.UserRepository;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceEntity;
import com.melly.vacationmanager.domain.vacation.balance.entity.VacationBalanceId;
import com.melly.vacationmanager.domain.vacation.balance.repository.VacationBalanceRepository;
import com.melly.vacationmanager.domain.vacation.grant.entity.VacationGrantEntity;
import com.melly.vacationmanager.domain.vacation.grant.repository.VacationGrantRepository;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.domain.vacation.type.repository.VacationTypeRepository;
import com.melly.vacationmanager.global.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacationGrantServiceImpl implements IVacationGrantService{

    private final VacationGrantRepository vacationGrantRepository;
    private final UserRepository userRepository;
    private final VacationTypeRepository vacationTypeRepository;
    private final VacationBalanceRepository vacationBalanceRepository;

    @Override
    public void recordGrant(UserEntity user, VacationTypeEntity type, Integer days) {
        VacationGrantEntity grant = VacationGrantEntity.builder()
                .user(user)
                .type(type)
                .grantDate(LocalDate.now())
                .grantedDays(days)
                .build();
        vacationGrantRepository.save(grant);
    }

    @Override
    @Transactional
    public void grantRegularVacations() {
        List<UserEntity> users = userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .filter(user -> user.getHireDate() != null)
                .toList();
        if (users.isEmpty()) {
            log.warn("[휴가 지급 스케줄러] 지급 대상자가 없습니다.");
            return;
        }

        List<VacationTypeEntity> types = vacationTypeRepository.findAll();
        if (types.isEmpty()) {
            log.warn("[휴가 지급 스케줄러] 지급 받을 휴가유형이 존재하지 않습니다.");
            return;
        }

        for (UserEntity user : users) {
            for (VacationTypeEntity type : types) {
                int grantDays = calculateDaysToGrant(user, type);

                VacationBalanceEntity balance = vacationBalanceRepository.findById(new VacationBalanceId(user.getUserId(), type.getTypeCode()))
                        .orElseGet(() -> VacationBalanceEntity.builder()
                                .id(new VacationBalanceId(user.getUserId(), type.getTypeCode()))
                                .user(user)
                                .type(type)
                                .remainingDays(BigDecimal.ZERO)
                                .updatedAt(LocalDateTime.now())
                                .build());

                balance.setRemainingDays(BigDecimal.valueOf(grantDays));
                vacationBalanceRepository.save(balance);

                recordGrant(user, type, grantDays);
            }
        }
    }

    private int calculateDaysToGrant(UserEntity user, VacationTypeEntity type) {
        if (!type.getTypeCode().equals("ANNUAL")) {
            // 기타 휴가 유형은 기본 지급
            return type.getDefaultDays();
        }

        LocalDate hireDate = user.getHireDate();
        long years = ChronoUnit.YEARS.between(hireDate, LocalDate.now());

        if (years < 1) {
            // 입사 첫 해: 월 비례 지급
            return calculateProRatedAnnual(hireDate);
        } else {
            int baseDays = 15;
            int extra = calculateAnnualBonusDays(years); // 명세 기반 가산
            return baseDays + extra;
        }
    }

    private int calculateAnnualBonusDays(long years) {
        if (years >= 7) return 3;
        if (years >= 5) return 2;
        if (years >= 3) return 1;
        return 0;
    }

    private Integer calculateProRatedAnnual(LocalDate joinDate) {
        // 입사월: 1월부터 12월까지
        int joinMonth = joinDate.getMonthValue();

        // 첫 해 비례 지급: 최대 11일 (1개월당 1일 지급)
        int monthsWorked = 12 - joinMonth + 1;

        // 최대 11일 제한
        return Math.min(monthsWorked, 11);
    }
}
