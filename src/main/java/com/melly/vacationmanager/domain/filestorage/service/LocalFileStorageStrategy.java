package com.melly.vacationmanager.domain.filestorage.service;

import com.melly.vacationmanager.global.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private final FileProperties fileProperties;

    @Override
    public String store(String directoryPath, String originalFilename, byte[] content) {
        try {
            File dir = new File(directoryPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String uniqueName = UUID.randomUUID().toString() + extension;
            Path targetPath = Path.of(directoryPath, uniqueName);
            Files.write(targetPath, content);

            return uniqueName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }

    @Override
    public Resource loadAsResource(String savedPath) {
        try {
            // URL에서 파일명만 추출
            String fileName = savedPath.substring(savedPath.lastIndexOf('/') + 1);

            Path filePath = Paths.get(fileProperties.getFullPath("evidence")).resolve(fileName).normalize();

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + savedPath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로 오류: " + savedPath, e);
        }
    }
}
