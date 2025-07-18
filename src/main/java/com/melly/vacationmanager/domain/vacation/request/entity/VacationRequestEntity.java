package com.melly.vacationmanager.domain.vacation.request.entity;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import com.melly.vacationmanager.global.common.enums.VacationRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="vacation_request_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VacationRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_code")
    private VacationTypeEntity vacationType;

    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    @Column(name="days_count")
    private BigDecimal daysCount;

    private String reason;

    @Enumerated(EnumType.STRING)
    private VacationRequestStatus status;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {  // null 체크 꼭 하기
            this.createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
