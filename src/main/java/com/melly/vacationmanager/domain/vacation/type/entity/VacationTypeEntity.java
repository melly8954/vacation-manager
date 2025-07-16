package com.melly.vacationmanager.domain.vacation.type.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vacation_type_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VacationTypeEntity {
    @Id
    @Column(name = "type_code")
    private String typeCode;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "default_days")
    private Integer defaultDays;

    @Column(name = "is_deductible")
    private Boolean isDeductible;

    @Column(name = "description")
    private String description;
}
