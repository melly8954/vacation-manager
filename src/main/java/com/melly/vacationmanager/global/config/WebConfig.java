package com.melly.vacationmanager.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 외부 디렉토리 매핑
        registry.addResourceHandler("/images/**") // URL 패턴 설정
                .addResourceLocations("file:///C:/vacation_manager/images/");      // 외부 디렉토리 경로 매핑
    }
}

