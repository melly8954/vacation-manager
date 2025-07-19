package com.melly.vacationmanager.domain.filestorage.service;

import com.melly.vacationmanager.domain.filestorage.repository.EvidenceFileRepository;
import com.melly.vacationmanager.global.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvidenceFileSerivceImpl implements IEvidenceFileService{
    private final FileProperties fileProperties;

    @Override
    public String saveEvidenceFile(String originalFilename, byte[] content) {
        try {
            File dir = new File(fileProperties.getEvidenceFiles());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex); // 확장자 포함
            }

            String uniqueName = UUID.randomUUID().toString() + extension;
            Path targetPath = Path.of(fileProperties.getEvidenceFiles(), uniqueName);
            Files.write(targetPath, content);

            return fileProperties.getAccessUrlBase() + "evidence_files/" + uniqueName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }
}
