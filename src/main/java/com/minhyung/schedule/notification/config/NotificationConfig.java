package com.minhyung.schedule.notification.config;

import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.handler.InviteNotificationOutboxEventHandler;
import com.minhyung.schedule.notification.handler.NotificationCreateEventHandler;
import com.minhyung.schedule.notification.props.NotifyLeaseProps;
import com.minhyung.schedule.notification.props.NotifyOutboxClaimerProps;
import com.minhyung.schedule.notification.repository.NotificationMessageRepository;
import com.minhyung.schedule.notification.repository.NotificationOutboxRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.service.*;
import com.minhyung.schedule.notification.worker.NotificationClaimer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@ConditionalOnProperty(name = "notify.mode", havingValue="outbox", matchIfMissing=true)
public class NotificationConfig {
    @Bean
    public NotificationCreateEventHandler notificationCreateEventHandler(
            @Qualifier("invitationNotificationOutboxService") NotificationService notificationService,
            PayloadEncoderRegistry payloadEncoderRegistry
    ) {
        return new InviteNotificationOutboxEventHandler(notificationService, payloadEncoderRegistry);
    }

    @Bean
    public NotificationService invitationNotificationOutboxService(NotificationRepository notificationRepository,
                                                                   NotificationOutboxRepository outboxRepository,
                                                                   NotificationMessageRepository messageRepository,
                                                                   UserService userService) {
        return new InvitationNotificationOutboxService(notificationRepository, outboxRepository, messageRepository, userService);
    }

    @Bean
    public NotificationClaimer notificationClaimer(@Qualifier("notificationQueuePublisher") QueuePublisher queuePublisher,
                                                   @Qualifier("notificationClaimerExecutor") ThreadPoolTaskExecutor executor,
                                                   NotifyOutboxClaimerProps props,
                                                   NotificationOutboxService outboxService) {
        return new NotificationClaimer(queuePublisher, executor, props, outboxService);
    }

    @Bean
    public NotificationOutboxService notificationOutboxService(NotificationOutboxRepository notificationOutboxRepository,
                                                               NotifyLeaseProps notifyLeaseProps) {
        return new NotificationOutboxService(notificationOutboxRepository, notifyLeaseProps);
    }

    @Bean
    public QueuePublisher notificationQueuePublisher(@Qualifier("notificationQueue") BlockingQueue<QueueMessage> queue) {
        return new InMemoryQueuePublisher(queue);
    }

    @Bean
    BlockingQueue<QueueMessage> notificationQueue() {
        return new LinkedBlockingQueue<>(50000);
    }

    @Bean
    public ThreadPoolTaskExecutor defaultAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationClaimerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notify-");
        executor.setWaitForTasksToCompleteOnShutdown(true);     // 작업 대기 후 종료
        executor.setAwaitTerminationSeconds(30);        // 대기 시간 30초
        executor.initialize();
        return executor;
    }
}
