package com.melly.vacationmanager.domain.filestorage.service;

public interface FileStorageStrategy {
    String store(String directoryPath, String originalFilename, byte[] content);
}
