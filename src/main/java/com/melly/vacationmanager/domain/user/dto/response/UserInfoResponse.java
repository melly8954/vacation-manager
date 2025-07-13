package com.melly.vacationmanager.domain.user.dto.response;

import com.melly.vacationmanager.global.common.enums.UserPosition;
import com.melly.vacationmanager.global.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String name;
    private String email;
    private LocalDate hireDate;
    private UserPosition position;
    private UserRole role;
    private LocalDateTime createdAt;

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
