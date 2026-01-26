package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("emailTaskExecutor")
    public Executor emailTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 🔧 Cấu hình Thread Pool
        executor.setCorePoolSize(3);           // 3 threads luôn sẵn sàng
        executor.setMaxPoolSize(10);           // Tối đa 10 threads khi busy
        executor.setQueueCapacity(50);         // Queue chứa 50 tasks chờ
        executor.setThreadNamePrefix("email-"); // Tên thread: email-1, email-2...
        executor.setKeepAliveSeconds(60);      // Thread tạm nghỉ sau 60s rảnh

        // Khi queue đầy → chạy trên thread gọi (không mất task)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("✅ Email Thread Pool initialized: core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }
}