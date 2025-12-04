package com.minhyung.schedule.notification.config;

import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.handler.InviteNotificationOutboxEventHandler;
import com.minhyung.schedule.notification.handler.NotificationCreateEventHandler;
import com.minhyung.schedule.notification.props.NotifyLeaseProps;
import com.minhyung.schedule.notification.props.NotifyOutboxClaimerProps;
import com.minhyung.schedule.notification.props.NotifyOutboxCommitterProps;
import com.minhyung.schedule.notification.repository.NotificationMessageRepository;
import com.minhyung.schedule.notification.repository.NotificationOutboxRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.service.*;
import com.minhyung.schedule.notification.worker.*;
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
    public NotificationSenderWorker notificationSenderWorker(@Qualifier("notificationQueue") BlockingQueue<QueueMessage> queue,
                                                             @Qualifier("notificationSenderExecutor") ThreadPoolTaskExecutor executor,
                                                             SseService sseService,
                                                             @Qualifier("successQueuePublisher") QueuePublisher successQueuePublisher,
                                                             @Qualifier("failureQueuePublisher") QueuePublisher failureQueuePublisher,
                                                             @Qualifier("retryQueuePublisher") QueuePublisher retryQueuePublisher
    ) {
        return new NotificationSenderWorker(queue, executor, sseService, successQueuePublisher, failureQueuePublisher, retryQueuePublisher);
    }

    @Bean
    public NotificationOutboxCommitter notificationSuccessCommitter(@Qualifier("notificationSuccessExecutor") ThreadPoolTaskExecutor executor,
                                                                    NotifyOutboxCommitterProps props,
                                                                    @Qualifier("successQueue") BlockingQueue<QueueMessage> successQueue,
                                                                    NotificationOutboxService outboxService) {
        return new NotificationSuccessCommitter(executor, props, successQueue, outboxService);
    }

    @Bean
    public NotificationOutboxCommitter notificationFailureCommitter(@Qualifier("notificationFailureExecutor") ThreadPoolTaskExecutor executor,
                                                                    NotifyOutboxCommitterProps props,
                                                                    @Qualifier("failureQueue") BlockingQueue<QueueMessage> failureQueue,
                                                                    NotificationOutboxService outboxService) {
        return new NotificationFailureCommitter(executor, props, failureQueue, outboxService);
    }

    @Bean
    public NotificationOutboxCommitter notificationRetryCommitter(@Qualifier("notificationRetryExecutor") ThreadPoolTaskExecutor executor,
                                                                  NotifyOutboxCommitterProps props,
                                                                  @Qualifier("retryQueue") BlockingQueue<QueueMessage> retryQueue,
                                                                  NotificationOutboxService outboxService,
                                                                  BackoffCalculator backoffCalculator) {
        return new NotificationRetryCommitter(executor, props, retryQueue, outboxService, backoffCalculator);
    }

    @Bean
    public QueuePublisher notificationQueuePublisher(@Qualifier("notificationQueue") BlockingQueue<QueueMessage> queue) {
        return new InMemoryQueuePublisher(queue);
    }

    @Bean
    BlockingQueue<QueueMessage> notificationQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public QueuePublisher successQueuePublisher(@Qualifier("successQueue") BlockingQueue<QueueMessage> queue) {
        return new InMemoryQueuePublisher(queue);
    }

    @Bean
    BlockingQueue<QueueMessage> successQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public QueuePublisher failureQueuePublisher(@Qualifier("failureQueue") BlockingQueue<QueueMessage> queue) {
        return new InMemoryQueuePublisher(queue);
    }

    @Bean
    BlockingQueue<QueueMessage> failureQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public QueuePublisher retryQueuePublisher(@Qualifier("retryQueue") BlockingQueue<QueueMessage> queue) {
        return new InMemoryQueuePublisher(queue);
    }

    @Bean
    BlockingQueue<QueueMessage> retryQueue() {
        return new LinkedBlockingQueue<>(500);
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
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("notify-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationSenderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("notify-sender-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationSuccessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("notify-success-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationFailureExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("notify-failure-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationRetryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("notify-retry-");
        executor.initialize();
        return executor;
    }
}
