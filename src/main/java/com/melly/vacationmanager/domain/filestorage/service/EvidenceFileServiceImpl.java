package com.melly.vacationmanager.domain.filestorage.service;

import com.melly.vacationmanager.domain.filestorage.dto.EvidenceFileDownloadDto;
import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import com.melly.vacationmanager.domain.filestorage.repository.EvidenceFileRepository;
import com.melly.vacationmanager.global.common.enums.ErrorCode;
import com.melly.vacationmanager.global.common.exception.CustomException;
import com.melly.vacationmanager.global.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EvidenceFileServiceImpl implements IEvidenceFileService{
    private final FileProperties fileProperties;
    private final FileStorageStrategy fileStorageStrategy;
    private final EvidenceFileRepository evidenceFileRepository;

    @Override
    public String saveEvidenceFile(String originalFilename, byte[] content) {
        try {
            String savedFilename = storeFile(originalFilename, content);
            return generateAccessUrl(savedFilename);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }

    @Override
    public EvidenceFileDownloadDto getFileForDownload(Long fileId) {
        EvidenceFileEntity entity = evidenceFileRepository.findByFileId(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        // 저장 경로 기반으로 Resource 생성
        Resource resource = fileStorageStrategy.loadAsResource(entity.getSavedPath());

        return new EvidenceFileDownloadDto(entity.getOriginalName(), entity.getFileType(), resource);
    }

    private String storeFile(String originalFilename, byte[] content) throws IOException {
        return fileStorageStrategy.store(fileProperties.getFullPath("evidence"), originalFilename, content);
    }

    private String generateAccessUrl(String savedFilename) {
        String baseUrl = fileProperties.getAccessUrlBase().replaceAll("/+$", ""); // 끝의 슬래시 모두 제거
        return String.format("%s/evidence_files/%s", baseUrl, savedFilename);
    }
}
