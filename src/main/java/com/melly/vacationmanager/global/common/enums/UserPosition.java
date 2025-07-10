package com.melly.vacationmanager.global.common.enums;

import lombok.Getter;

@Getter
public enum UserPosition {
    STAFF("사원"),
    ASSISTANT_MANAGER("대리"),
    MANAGER("과장"),
    SENIOR_MANAGER("차장"),
    DIRECTOR("부장");

    private final String description;

    UserPosition(String description) {
        this.description = description;
    }

}
