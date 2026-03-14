package com.sch.capstone.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// 비동기 작업을 실행할 때 사용할 스레드 풀 정의해주는 설정 클래스
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "uploadTaskExecutor")
    public Executor uploadTaskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        
        ex.setCorePoolSize(4); // 기본적으로 유지할 스레드 수
        ex.setMaxPoolSize(8); // 동시에 실행 가능한 최대 스레드 수
        ex.setQueueCapacity(100); // 큐에 쌓아둘 수 있는 작업 개수

        ex.setThreadNamePrefix("upload-"); // 스레드 이름 접두사에 upload 붙이기
        
        ex.setWaitForTasksToCompleteOnShutdown(true); // 애플리케이션 종료 시, 대기 중인 작업이 끝날 때까지 기다림
        ex.setAwaitTerminationSeconds(30); // 최대 30초까지 대기 후 강제 종료
        
        // 큐와 스레드가 꽉 찼을 때 요청한 쓰레드가 작업을 직접 작업을 실행(새 스레드 만들지 않고)
        ex.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        ex.initialize(); // ThreadPoolTaskExecutor 객체 초기화
        return ex;
    }
}