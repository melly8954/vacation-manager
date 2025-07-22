package com.melly.vacationmanager.domain.filestorage.service;

import java.io.IOException;

public interface FileStorageStrategy {
    String store(String directoryPath, String originalFilename, byte[] content) throws IOException;
}
