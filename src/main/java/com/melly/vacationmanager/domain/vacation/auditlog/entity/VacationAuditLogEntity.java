package com.melly.vacationmanager.domain.vacation.auditlog.entity;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.request.entity.VacationRequestEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vacation_audit_log_tbl")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VacationAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private VacationRequestEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private UserEntity changedBy;

    @Column(name = "changed_by_role")
    private String changedByRole;

    @Column(name = "change_date")
    private LocalDateTime changeDate;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "comment")
    private String comment;

    @PrePersist
    public void prePersist() {
        if (this.changeDate == null) {
            this.changeDate = LocalDateTime.now();
        }
    }
}
