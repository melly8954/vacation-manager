package com.melly.vacationmanager.domain.vacation.request.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VacationRequestSearchCond {
    private Long userId;        // 인증된 사용자 ID

    private String typeCode;    // 휴가 유형 필터 (예: "ANNUAL", "SICK", "ALL")
    private String status;      // 휴가 상태 필터 (예: "PENDING", "APPROVED", "ALL")
    private String year;        // 신청 연도 (예: "2025", "ALL")
    private String month;       // 신청 월 (예: "7", "ALL")
    private String order;       // 정렬 방향 ("asc", "desc")
    private String dateFilterType;

    private int page;           // 페이지 번호 (0-based 처리 예정)
    private int size;           // 페이지 크기
}
