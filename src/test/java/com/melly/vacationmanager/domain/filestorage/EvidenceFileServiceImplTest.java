package com.melly.vacationmanager.domain.filestorage;

import com.melly.vacationmanager.domain.filestorage.service.EvidenceFileServiceImpl;
import com.melly.vacationmanager.domain.filestorage.service.FileStorageStrategy;
import com.melly.vacationmanager.global.config.FileProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceFileServiceImplTest {
    @TempDir    //  테스트 전용 임시 디렉토리에 파일을 생성
    Path tempDir;

    @Mock FileStorageStrategy fileStorageStrategy;
    @Mock FileProperties fileProperties;

    @InjectMocks
    EvidenceFileServiceImpl evidenceFileService;

    @Test
    @DisplayName("정상 흐름 - 파일이 저장되고 접근 가능한 URL 반환")
    void saveEvidenceFile_success() throws IOException {
        String originalName = "test_document.pdf";
        byte[] content = "샘플 파일 내용".getBytes();

        // fileProperties 셋업
        when(fileProperties.getAccessUrlBase()).thenReturn("http://localhost:8081/files");
        when(fileProperties.getFullPath("evidence")).thenReturn(tempDir.resolve("evidence_files").toString());

        // fileStorageStrategy.store() 가 실제 파일명으로 리턴하도록 임의값 세팅
        when(fileStorageStrategy.store(anyString(), anyString(), any())).thenAnswer(invocation -> {
            String dir = invocation.getArgument(0);
            String originalFileName = invocation.getArgument(1);
            // 그냥 UUID 같은 임의 이름 생성 or 원본파일명 리턴
            return "stored-" + originalFileName;
        });

        String resultUrl = evidenceFileService.saveEvidenceFile(originalName, content);

        assertThat(resultUrl).startsWith("http://localhost:8081/files/evidence_files/");
        assertThat(resultUrl).contains("stored-" + originalName);
    }

    @Test
    @DisplayName("예외 흐름 - 파일 저장 중 IOException 반환")
    void saveEvidenceFile_throwsIOException() throws IOException {
        String originalName = "fail_document.pdf";
        byte[] content = "내용".getBytes();

        // fileProperties 셋업 (실제 값은 크게 중요하지 않음)
        when(fileProperties.getFullPath("evidence")).thenReturn(tempDir.toString());

        // fileStorageStrategy.store() 호출 시 IOException 강제 발생
        when(fileStorageStrategy.store(anyString(), anyString(), any()))
                .thenThrow(new IOException("강제 IOException 발생"));

        // 실행 및 예외 검증
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            evidenceFileService.saveEvidenceFile(originalName, content);
        });

        // 예외 메시지와 원인 IOException 여부 확인
        assertThat(thrown).hasMessageContaining("파일 저장 중 오류 발생");
        assertThat(thrown.getCause()).isInstanceOf(IOException.class)
                .hasMessageContaining("강제 IOException 발생");
    }
}
