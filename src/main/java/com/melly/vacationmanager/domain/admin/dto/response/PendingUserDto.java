package com.melly.vacationmanager.domain.admin.dto.response;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
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
    private LocalDateTime createdAt;
    private String status;

    public static PendingUserDto from(UserEntity user) {
        return new PendingUserDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getStatus().name()
        );
    }
}
