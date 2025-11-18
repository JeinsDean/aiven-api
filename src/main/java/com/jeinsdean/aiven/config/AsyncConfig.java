package com.jeinsdean.aiven.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리 설정
 *
 * AI API 호출은 응답 시간이 길어서(수 초) 비동기 필수
 * 대규모 트래픽 시 스레드 풀 튜닝이 성능의 핵심
 *
 * 스레드 풀 크기 계산:
 * - CPU 집약: 코어 수 + 1
 * - I/O 집약(AI API): 코어 수 * 2~4 (대기 시간이 많음)
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * AI API 호출 전용 스레드 풀
     * I/O 바운드 작업에 최적화
     */
    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 스레드 수 (항상 유지)
        executor.setCorePoolSize(10);

        // 최대 스레드 수 (트래픽 증가 시)
        executor.setMaxPoolSize(50);

        // 큐 용량 (대기열)
        // 큐가 가득 차면 maxPoolSize까지 스레드 생성
        executor.setQueueCapacity(100);

        // 유휴 스레드 대기 시간 (초과 시 종료)
        executor.setKeepAliveSeconds(60);

        // 스레드 이름 접두사 (로그 추적 용이)
        executor.setThreadNamePrefix("ai-async-");

        // 거부 정책: 호출 스레드에서 직접 실행
        // AI API는 실패하면 안 되므로 CallerRunsPolicy 사용
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 애플리케이션 종료 시 처리
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        log.info("AI Task Executor initialized: core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }

    /**
     * 일반 비동기 작업 전용 스레드 풀
     * 알림 발송, 로깅 등
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }
}