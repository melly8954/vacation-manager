package com.melly.vacationmanager.domain.filestorage.service;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFileStorageStrategy implements FileStorageStrategy {

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
}
