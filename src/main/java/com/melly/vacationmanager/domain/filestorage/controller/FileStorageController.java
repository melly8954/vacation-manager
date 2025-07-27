package com.melly.vacationmanager.domain.filestorage.controller;

import com.melly.vacationmanager.domain.filestorage.dto.EvidenceFileDownloadDto;
import com.melly.vacationmanager.domain.filestorage.service.IEvidenceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files/evidence")
public class FileStorageController {


    private final IEvidenceFileService evidenceFileService;

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadEvidenceFile(@PathVariable Long fileId) {
        EvidenceFileDownloadDto fileDto = evidenceFileService.getFileForDownload(fileId);
        String originalFilename = fileDto.getOriginalName();
        String encodedFilename = null;
        try {
            encodedFilename = URLEncoder.encode(originalFilename, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            encodedFilename = "file";
        }

        String contentDisposition = String.format(
                "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                // ASCII만 허용되며, 한글 등 있다면 fallback 명칭(예: "file.pdf")을 줄 것
                originalFilename.replaceAll("[^\\x20-\\x7E]", "_"),
                encodedFilename
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(fileDto.getResource());
    }
}
