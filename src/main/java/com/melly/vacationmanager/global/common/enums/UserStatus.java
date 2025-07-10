package com.melly.vacationmanager.global.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    PENDING("승인 대기"),
    ACTIVE("승인 완료"),
    REJECTION("승인 반려");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
