package com.melly.vacationmanager.domain.admin.user.dto;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import com.melly.vacationmanager.global.common.enums.UserPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PendingUserDto {
    private Long userId;
    private String name;
    private String email;
    private UserPosition position;
    private LocalDateTime createdAt;
    private String status;

    public static PendingUserDto from(UserEntity user) {
        return new PendingUserDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPosition(),
                user.getCreatedAt(),
                user.getStatus().name()
        );
    }
}
