package com.melly.vacationmanager.domain.admin.dto.response;

import com.melly.vacationmanager.domain.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AdminUserManagePendingPageResponse {
    private List<PendingUserDto> content; // 사용자 목록
    private int page;                     // 현재 페이지 번호 (1-based)
    private int size;                     // 페이지 크기
    private long totalElements;           // 전체 데이터 개수
    private int totalPages;               // 전체 페이지 개수
    private boolean last;                 // 마지막 페이지 여부


    // 편의 메서드: Page<UserEntity> -> DTO 변환 메서드
    public static AdminUserManagePendingPageResponse from(Page<UserEntity> page) {
        List<PendingUserDto> dtoList = page.getContent().stream()
                .map(PendingUserDto::from)
                .toList();

        return new AdminUserManagePendingPageResponse(
                dtoList,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
