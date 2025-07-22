package com.melly.vacationmanager.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    private String accessUrlBase;
    private String storagePath; // 최상위 저장소 루트
    private Map<String, String> directories; // 서브 디렉토리 구분 (evidence, profile 등)

    public String getFullPath(String typeKey) {
        return Paths.get(storagePath, directories.get(typeKey)).toString();
    }
}