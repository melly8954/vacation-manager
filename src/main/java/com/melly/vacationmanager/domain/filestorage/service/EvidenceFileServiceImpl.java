package com.melly.vacationmanager.domain.filestorage.service;

import com.melly.vacationmanager.global.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EvidenceFileServiceImpl implements IEvidenceFileService{
    private final FileProperties fileProperties;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public String saveEvidenceFile(String originalFilename, byte[] content) {
        try {
            String savedFilename = storeFile(originalFilename, content);
            return generateAccessUrl(savedFilename);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }

    private String storeFile(String originalFilename, byte[] content) throws IOException {
        return fileStorageStrategy.store(fileProperties.getFullPath("evidence"), originalFilename, content);
    }

    private String generateAccessUrl(String savedFilename) {
        String baseUrl = fileProperties.getAccessUrlBase().replaceAll("/+$", ""); // 끝의 슬래시 모두 제거
        return String.format("%s/evidence_files/%s", baseUrl, savedFilename);
    }
}
