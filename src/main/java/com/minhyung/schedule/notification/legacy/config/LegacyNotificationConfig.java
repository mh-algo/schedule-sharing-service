package com.minhyung.schedule.notification.legacy.config;

import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.legacy.handler.LegacyInviteNotificationCreateEventHandler;
import com.minhyung.schedule.notification.handler.NotificationCreateEventHandler;
import com.minhyung.schedule.notification.legacy.handler.LegacyNotificationPreparedEventHandler;
import com.minhyung.schedule.notification.legacy.service.LegacyInvitationNotificationService;
import com.minhyung.schedule.notification.legacy.service.LegacyNotificationStatusService;
import com.minhyung.schedule.notification.props.NotifyRetryProps;
import com.minhyung.schedule.notification.repository.NotificationMessageRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.repository.NotificationSendingRepository;
import com.minhyung.schedule.notification.legacy.scheduler.LegacyNotificationRetryScheduler;
import com.minhyung.schedule.notification.service.*;
import com.minhyung.schedule.notification.legacy.worker.LegacyNotificationWorker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Deprecated(forRemoval = true)
@Configuration
@ConditionalOnProperty(name = "notify.mode", havingValue="legacy")
public class LegacyNotificationConfig {
    @Bean
    public NotificationCreateEventHandler notificationCreateEventHandler(@Qualifier("invitationNotificationService") NotificationService notificationService,
                                                                                                 ApplicationEventPublisher publisher,
                                                                                                 PayloadEncoderRegistry payloadEncoderRegistry) {
        return new LegacyInviteNotificationCreateEventHandler(notificationService, publisher, payloadEncoderRegistry);
    }

    @Bean
    public NotificationService invitationNotificationService(NotificationRepository notificationRepository,
                                                             NotificationSendingRepository sendingRepository,
                                                             NotificationMessageRepository messageRepository,
                                                             UserService userService) {
        return new LegacyInvitationNotificationService(notificationRepository, sendingRepository, messageRepository, userService);
    }

    @Bean
    public LegacyNotificationPreparedEventHandler notificationPreparedEventHandler(@Qualifier("queuePublisher") QueuePublisher publisher,
                                                                                   LegacyNotificationStatusService legacyNotificationStatusService,
                                                                                   BackoffCalculator backoffCalculator) {
        return new LegacyNotificationPreparedEventHandler(publisher, legacyNotificationStatusService, backoffCalculator);
    }

    @Bean
    public LegacyNotificationWorker notificationWorker(@Qualifier("notificationQueue") BlockingQueue<QueueMessage> queue,
                                                       @Qualifier("notificationExecutor") ThreadPoolTaskExecutor executor,
                                                       SseService sseService,
                                                       LegacyNotificationStatusService legacyNotificationStatusService,
                                                       BackoffCalculator backoffCalculator) {
        return new LegacyNotificationWorker(queue, executor, sseService, legacyNotificationStatusService, backoffCalculator);
    }

    @Bean
    public LegacyNotificationRetryScheduler notificationRetryScheduler(@Qualifier("retryScheduler") ThreadPoolTaskScheduler scheduler,
                                                                       LegacyNotificationStatusService legacyNotificationStatusService,
                                                                       NotifyRetryProps props,
                                                                       @Qualifier("queuePublisher") QueuePublisher publisher,
                                                                       BackoffCalculator backoffCalculator) {
        return new LegacyNotificationRetryScheduler(scheduler, legacyNotificationStatusService, props, publisher, backoffCalculator);
    }

    @Bean
    public LegacyNotificationStatusService notificationStatusService(NotificationSendingRepository sendingRepository, NotificationRepository notificationRepository) {
        return new LegacyNotificationStatusService(sendingRepository, notificationRepository);
    }

    @Bean
    public QueuePublisher queuePublisher(@Qualifier("notificationQueue") BlockingQueue<QueueMessage> queue) {
        return new InMemoryQueuePublisher(queue);
    }

    @Bean
    BlockingQueue<QueueMessage> notificationQueue() {
        return new LinkedBlockingQueue<>(50000);
    }

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
}
