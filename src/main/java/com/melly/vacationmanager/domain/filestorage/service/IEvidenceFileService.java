package com.melly.vacationmanager.domain.filestorage.service;

public interface IEvidenceFileService {
    String saveEvidenceFile(String originalFilename, byte[] content);
}
