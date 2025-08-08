package com.melly.vacationmanager.global.common.enums;

import com.melly.vacationmanager.global.common.exception.CustomException;
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

    public static UserPosition fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new CustomException(ErrorCode.POSITION_MISSING);
        }

        // 1) 먼저 이름 기준 변환 시도 (대소문자 무시)
        try {
            return UserPosition.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 2) 이름 변환 실패 시 description 기준 탐색
            for (UserPosition pos : values()) {
                if (pos.getDescription().equals(value)) {
                    return pos;
                }
            }
            // 못찾으면 예외 던짐
            throw new IllegalArgumentException("유효하지 않은 UserPosition 값: " + value);
        }
    }
}
