package com.melly.vacationmanager.domain.vacation.grant.entity;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.domain.vacation.type.entity.VacationTypeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "vacation_grant_tbl")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationGrantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grant_id")
    private Long grantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_code")
    private VacationTypeEntity type;

    @Column(name = "grant_date")
    private LocalDate grantDate;

    @Column(name = "granted_days")
    private Integer grantedDays;
}
