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

    private Integer calculateDaysToGrant(UserEntity user, VacationTypeEntity type) {
        if (type.getTypeCode().equals("ANNUAL")) {
            int years = (int) ChronoUnit.YEARS.between(user.getHireDate(), LocalDate.now());

            if (years == 0) {
                return calculateProRatedAnnual(user.getHireDate()); // BigDecimal 반환 가정
            } else {
                int baseDays = 15;
                int extraDays = (years >= 15) ? 10 : (years - 1); // 최대 25일까지
                return baseDays + extraDays;
            }
        }

        // 기타 휴가 유형은 기본 일수(BigDecimal) 반환
        return type.getDefaultDays();
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
