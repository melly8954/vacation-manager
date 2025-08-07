package com.melly.vacationmanager.domain.vacation.request.dto.response;

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
public class VacationRequestPageResponse {

    private List<VacationRequestListResponse> vacationRequests;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static VacationRequestPageResponse from(Page<VacationRequestListResponse> page) {
        return VacationRequestPageResponse.builder()
                .vacationRequests(page.getContent())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
