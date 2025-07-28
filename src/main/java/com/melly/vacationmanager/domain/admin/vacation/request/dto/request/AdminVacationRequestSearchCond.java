package com.melly.vacationmanager.domain.admin.vacation.request.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminVacationRequestSearchCond {
    private int page;           // 페이지 번호
    private int size;           // 페이지 크기

    private String name;           // 사용자 이름
    private String typeCode;       // 휴가 유형 코드
    private String status;         // 휴가 상태 코드
    private String year;          // 연도
    private String month;         // 월
    private String order;          // asc / desc
}
