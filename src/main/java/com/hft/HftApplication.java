package com.hft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class HftApplication {
    public static void main(String[] args) {
        SpringApplication.run(HftApplication.class, args);
        System.out.println("\n=== HFT Phase 2 Matching Engine Started on Port 8080 ===\n");
    }

    @Bean(name = "taskExecutor")
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("HFT-Async-");
        executor.initialize();
        return executor;
    }
}
