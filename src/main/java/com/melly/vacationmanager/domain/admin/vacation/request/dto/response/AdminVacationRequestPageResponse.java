package com.melly.vacationmanager.domain.admin.vacation.request.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminVacationRequestPageResponse {

    private List<AdminVacationRequestListResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static AdminVacationRequestPageResponse fromPage(Page<AdminVacationRequestListResponse> page) {
        return new AdminVacationRequestPageResponse(
                page.getContent(),
                page.getNumber()+1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
