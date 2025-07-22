package com.melly.vacationmanager.domain.filestorage;

import com.melly.vacationmanager.domain.filestorage.service.EvidenceFileSerivceImpl;
import com.melly.vacationmanager.global.config.FileProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class EvidenceFileServiceImplTest {

    @TempDir    //  테스트 전용 임시 디렉토리에 파일을 생성
    Path tempDir;

    @Test
    @DisplayName("정상 흐름 - 파일이 저장되고 접근 가능한 URL 반환")
    void saveEvidenceFile_success() throws IOException {
        // given
        FileProperties properties = new FileProperties();
        properties.setEvidenceFiles(tempDir.toString());
        properties.setAccessUrlBase("http://localhost:8081/images/");

        EvidenceFileSerivceImpl service = new EvidenceFileSerivceImpl(properties);

        String originalName = "test_document.pdf";
        byte[] content = "샘플 파일 내용".getBytes();

        // when
        String resultUrl = service.saveEvidenceFile(originalName, content);

        // then
        assertThat(resultUrl).startsWith("http://localhost:8081/images/evidence_files/");

        // 실제로 파일이 생성되었는지 확인
        String filename = resultUrl.substring(resultUrl.lastIndexOf('/') + 1);
        Path savedFilePath = tempDir.resolve(filename);
        assertThat(Files.exists(savedFilePath)).isTrue();

        // 저장된 파일 내용 검증 (선택)
        byte[] savedContent = Files.readAllBytes(savedFilePath);
        assertThat(savedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("예외 흐름 - 파일 저장 중 IOException 반환")
    void saveEvidenceFile_throwsIOException() {
        // given
        FileProperties properties = new FileProperties();
        // 잘못된 경로 설정으로 강제 오류 유발 (윈도우에서 불가능한 경로 문자 사용 등)
        properties.setEvidenceFiles("?:/invalid_path");
        properties.setAccessUrlBase("http://localhost:8081/images/");

        EvidenceFileSerivceImpl service = new EvidenceFileSerivceImpl(properties);

        String originalName = "bad_file.txt";
        byte[] content = "에러 발생용".getBytes();

        // when & then
        assertThatThrownBy(() -> service.saveEvidenceFile(originalName, content))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 저장 중 오류 발생");
    }
}
