package com.jeinsdean.aiven.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 *
 * 향후 추가될 설정:
 * - Interceptor (로깅, Rate Limiting)
 * - Argument Resolver (커스텀 파라미터 바인딩)
 * - Message Converter (커스텀 직렬화)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 현재는 기본 설정 사용
    // Phase 4에서 Rate Limiting Interceptor 추가 예정
}