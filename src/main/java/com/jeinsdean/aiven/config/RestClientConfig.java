package com.jeinsdean.aiven.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * HTTP 클라이언트 설정
 *
 * RestClient vs WebClient:
 * - RestClient: 동기 방식, 간단한 API 호출
 * - WebClient: 비동기/논블로킹, AI API처럼 오래 걸리는 호출
 *
 * AI API는 WebClient 사용 (CompletableFuture와 조합)
 */
@Configuration
public class RestClientConfig {

    /**
     * 동기 RestClient (일반 API 호출용)
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    /**
     * 비동기 WebClient (AI API 호출용)
     * 논블로킹 I/O로 스레드 효율 극대화
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        // 응답 크기 제한 (10MB)
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * HTTP 요청 팩토리 설정
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Connection Timeout: 5초
        factory.setConnectTimeout(Duration.ofSeconds(5));

        // Read Timeout: 30초 (AI API는 느릴 수 있음)
        factory.setReadTimeout(Duration.ofSeconds(30));

        return factory;
    }
}