package com.melly.vacationmanager.domain.vacation.request.dto.response;

import com.melly.vacationmanager.domain.filestorage.entity.EvidenceFileEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvidenceFileResponse {
    private Long fileId;
    private String originalName;
    private String fileType;
    private long fileSize;
    private String uploadedAt;
    private String downloadUrl;

    public static EvidenceFileResponse from(EvidenceFileEntity entity) {
        return new EvidenceFileResponse(
                entity.getFileId(),
                entity.getOriginalName(),
                entity.getFileType(),
                entity.getFileSize(),
                entity.getUploadedAt().toString(),  // LocalDateTime → String 변환 필요
                "/api/v1/files/evidence/" + entity.getFileId() + "/download"
        );
    }
}
