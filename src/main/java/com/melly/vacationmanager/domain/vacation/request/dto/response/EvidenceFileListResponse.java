package com.melly.vacationmanager.domain.vacation.request.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class EvidenceFileListResponse {
    private List<EvidenceFileResponse> evidenceFiles;
}