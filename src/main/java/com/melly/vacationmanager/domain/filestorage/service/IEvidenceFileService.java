package com.melly.vacationmanager.domain.filestorage.service;

import com.melly.vacationmanager.domain.filestorage.dto.EvidenceFileDownloadDto;

public interface IEvidenceFileService {
    String saveEvidenceFile(String originalFilename, byte[] content);

    EvidenceFileDownloadDto getFileForDownload(Long fileId);

}
