package com.melly.vacationmanager.domain.vacation.balance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VacationBalanceId implements Serializable {
    // 반드시 equals()/hashCode()를 override해야 Hibernate가 복합키를 인식한다.
    @Column(name = "user_id")
    private Long userId;

    @Setter
    @Column(name = "type_code")
    private String typeCode;
}
