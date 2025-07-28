package com.melly.vacationmanager.domain.vacation.balance.entity;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="vacation_balance_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VacationBalanceEntity {
    @EmbeddedId
    private VacationBalanceId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @MapsId("typeCode")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_code", nullable = false)
    private VacationTypeEntity type;

    @Setter
    @Column(name = "remaining_days")
    private BigDecimal remainingDays;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
