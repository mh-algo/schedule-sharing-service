package com.minhyung.schedule.notification.config;

import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.handler.InviteNotificationCreateEventHandler;
import com.minhyung.schedule.notification.handler.NotificationCreateEventHandler;
import com.minhyung.schedule.notification.handler.NotificationPreparedEventHandler;
import com.minhyung.schedule.notification.props.NotifyRetryProps;
import com.minhyung.schedule.notification.repository.NotificationMessageRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.repository.NotificationSendingRepository;
import com.minhyung.schedule.notification.scheduler.NotificationRetryScheduler;
import com.minhyung.schedule.notification.service.*;
import com.minhyung.schedule.notification.worker.NotificationWorker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@ConditionalOnProperty(name = "notify.mode", havingValue="legacy", matchIfMissing=true)
public class LegacyNotificationConfig {
    @Bean
    public NotificationCreateEventHandler notificationCreateEventHandler(@Qualifier("invitationNotificationService") NotificationService notificationService,
                                                                                                 ApplicationEventPublisher publisher,
                                                                                                 PayloadEncoderRegistry payloadEncoderRegistry) {
        return new InviteNotificationCreateEventHandler(notificationService, publisher, payloadEncoderRegistry);
    }

    @Bean
    public NotificationService invitationNotificationService(NotificationRepository notificationRepository,
                                                             NotificationSendingRepository sendingRepository,
                                                             NotificationMessageRepository messageRepository,
                                                             UserService userService) {
        return new InvitationNotificationService(notificationRepository, sendingRepository, messageRepository, userService);
    }

    @Bean
    public NotificationPreparedEventHandler notificationPreparedEventHandler(QueuePublisher queuePublisher,
                                                                             NotificationStatusService notificationStatusService,
                                                                             BackoffCalculator backoffCalculator) {
        return new NotificationPreparedEventHandler(queuePublisher, notificationStatusService, backoffCalculator);
    }

    @Bean
    public NotificationWorker notificationWorker(@Qualifier("notificationQueue") BlockingQueue<QueueMessage> queue,
                                                 @Qualifier("notificationExecutor") ThreadPoolTaskExecutor executor,
                                                 SseService sseService,
                                                 NotificationStatusService notificationStatusService,
                                                 BackoffCalculator backoffCalculator) {
        return new NotificationWorker(queue, executor, sseService, notificationStatusService, backoffCalculator);
    }

    @Bean
    public NotificationRetryScheduler notificationRetryScheduler(@Qualifier("retryScheduler") ThreadPoolTaskScheduler scheduler,
                                                                 NotificationStatusService notificationStatusService,
                                                                 NotifyRetryProps props,
                                                                 QueuePublisher publisher,
                                                                 BackoffCalculator backoffCalculator) {
        return new NotificationRetryScheduler(scheduler, notificationStatusService, props, publisher, backoffCalculator);
    }

    @Bean
    public QueuePublisher queuePublisher(@Qualifier("notificationQueue") BlockingQueue<QueueMessage> queue) {
        return new InMemoryNotificationQueuePublisher(queue);
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
