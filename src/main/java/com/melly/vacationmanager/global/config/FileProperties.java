package com.melly.vacationmanager.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    private String accessUrlBase;
    private String evidenceFiles;
}