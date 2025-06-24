package com.kdt.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 로컬 경로: 현재 프로젝트의 /uploads 디렉토리
        String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();

        // 기본 정적 리소스 설정
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        // ✅ /api/image/** 매핑 제거 - ImageController에서 처리
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /api, /uploads, /assets, /static 등은 SPA 포워딩에서 제외
        registry.addViewController("/{spring:(?!api|uploads|assets|static)[^\\.]*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:(?!api|uploads|assets|static)[^\\.]*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:(?!api|uploads|assets|static)[^\\.]*}/**{spring:?!(\\.js|\\.css)$}")
                .setViewName("forward:/index.html");
    }
}
