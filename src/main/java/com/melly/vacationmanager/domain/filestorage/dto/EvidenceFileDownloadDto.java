package com.melly.vacationmanager.domain.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class EvidenceFileDownloadDto {
    private final String originalName;
    private final String fileType;
    private final Resource resource;
}
