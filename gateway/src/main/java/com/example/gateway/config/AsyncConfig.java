package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);                   // 常驻 2 线程
        executor.setMaxPoolSize(4);                    // 最大 4 线程
        executor.setQueueCapacity(200);                // 缓冲队列
        executor.setKeepAliveSeconds(60);              // 空闲线程存活时间
        executor.setThreadNamePrefix("log-async-");    // 线程名前缀
        executor.setRejectedExecutionHandler(          // 队列满时丢弃最旧的
                new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
