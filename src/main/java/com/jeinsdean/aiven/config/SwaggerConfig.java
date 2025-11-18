package com.jeinsdean.aiven.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3.0 설정
 *
 * API 문서 자동 생성으로 프론트엔드/모바일 팀과의 협업 효율 향상
 * 접속: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 인증 스키마 정의
        String jwtSchemeName = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요. (Bearer 제외)")
                );

        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("Aiven API")
                .description("""
                        AI 기반 맞춤형 제안 서비스 API
                        
                        ## 주요 기능
                        - 시간/장소 기반 AI 제안
                        - 사용자 맞춤 설정
                        - 피드백 학습
                        
                        ## 인증
                        - JWT Bearer Token 방식
                        - Authorization 헤더에 토큰 포함
                        """)
                .version("v1.0.0");
    }

    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Server"),
                new Server()
                        .url("https://api-dev.aiven.com")
                        .description("Development Server"),
                new Server()
                        .url("https://api.aiven.com")
                        .description("Production Server")
        );
    }
}