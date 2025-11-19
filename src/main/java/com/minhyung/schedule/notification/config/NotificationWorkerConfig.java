package com.minhyung.schedule.notification.config;

import com.minhyung.schedule.notification.domain.QueueMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class NotificationWorkerConfig {
    @Bean
    public ThreadPoolTaskExecutor notificationExecutor() {
//        int cores = Runtime.getRuntime().availableProcessors();     // I/O 바운드 작업 기준 스레드 개수 = CPU 코어수 * 2
//        int n = Math.max(8, Math.min(cores*2, 16));                // 최소 8, 최대 16
        int n = 8;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(n);
        executor.setMaxPoolSize(n);
        executor.setQueueCapacity(0);       // 별도의 메시지 큐를 사용하기 때문에 0으로 설정
        executor.setThreadNamePrefix("notify-");
        executor.setWaitForTasksToCompleteOnShutdown(true);     // 작업 대기 후 종료
        executor.setAwaitTerminationSeconds(30);        // 대기 시간 30초
        executor.initialize();
        return executor;
    }

    @Bean
    BlockingQueue<QueueMessage> notificationQueue() {
        return new LinkedBlockingQueue<>(50000);
    }

    @Bean
    public ThreadPoolTaskScheduler retryScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("retry-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    @Bean(name = "defaultAsyncExecutor")
    public ThreadPoolTaskExecutor defaultAsyncExecutor() {
//        int cores = Runtime.getRuntime().availableProcessors();     // I/O 바운드 작업 기준 스레드 개수 = CPU 코어수 * 2
//        int n = Math.max(8, Math.min(cores*2, 16));                // 최소 8, 최대 16
        int n = 8;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(n);
        executor.setMaxPoolSize(n);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
