package com.minhyung.schedule.notification.config;

import com.minhyung.schedule.log.AsyncExecStats;
import com.minhyung.schedule.notification.domain.NotificationCreateQueueMessage;
import com.minhyung.schedule.notification.domain.NotificationQueueMessage;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.handler.LegacyInviteNotificationOutboxEventHandler;
import com.minhyung.schedule.notification.handler.NotificationCreateEventHandler;
import com.minhyung.schedule.notification.handler.InviteNotificationOutboxBatchEventHandler;
import com.minhyung.schedule.notification.props.NotifyLeaseProps;
import com.minhyung.schedule.notification.props.NotifyOutboxClaimerProps;
import com.minhyung.schedule.notification.props.NotifyOutboxCommitterProps;
import com.minhyung.schedule.notification.props.NotifyOutboxCreateProps;
import com.minhyung.schedule.notification.repository.NotificationOutboxJdbcRepository;
import com.minhyung.schedule.notification.repository.NotificationOutboxRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.service.*;
import com.minhyung.schedule.notification.worker.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "notify.mode", havingValue="outbox", matchIfMissing=true)
public class NotificationConfig {
    @Bean
    public NotificationCreateEventHandler notificationCreateBatchEventHandler(
            @Qualifier("inviteNotificationQueuePublisher") QueuePublisher<NotificationCreateQueueMessage> queuePublisher,
            PayloadEncoderRegistry payloadEncoderRegistry
    ) {
        return new InviteNotificationOutboxBatchEventHandler(queuePublisher, payloadEncoderRegistry);
    }

    @Deprecated
//    @Bean
    public NotificationCreateEventHandler notificationCreateLegacyEventHandler(
            @Qualifier("legacyInvitationNotificationOutboxService") NotificationService notificationService,
            PayloadEncoderRegistry payloadEncoderRegistry
    ) {
        return new LegacyInviteNotificationOutboxEventHandler(notificationService, payloadEncoderRegistry);
    }

    @Deprecated
//    @Bean
    public NotificationService legacyInvitationNotificationOutboxService(NotificationRepository notificationRepository,
                                                                         NotificationOutboxRepository outboxRepository,
                                                                         EntityManager em) {
        return new LegacyInvitationNotificationOutboxService(notificationRepository, outboxRepository, em);
    }

    @Bean
    public NotificationCreateWorker notificationCreateWorker(@Qualifier("inviteNotificationQueue") BlockingQueue<NotificationCreateQueueMessage> notificationQueue,
                                                             @Qualifier("notificationCreateExecutor") ThreadPoolTaskExecutor executor,
                                                             NotifyOutboxCreateProps props,
                                                             NotificationOutboxJdbcRepository notificationOutboxJdbcRepository) {
        return new NotificationCreateWorker(notificationQueue, executor, props, notificationOutboxJdbcRepository);
    }

    @Bean
    public NotificationClaimer notificationClaimer(@Qualifier("notificationQueuePublisher") QueuePublisher<NotificationQueueMessage> queuePublisher,
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
    public NotificationSenderWorker notificationSenderWorker(@Qualifier("notificationQueue") BlockingQueue<NotificationQueueMessage> queue,
                                                             @Qualifier("notificationSenderExecutor") ThreadPoolTaskExecutor executor,
                                                             SseService sseService,
                                                             @Qualifier("successQueuePublisher") QueuePublisher<NotificationQueueMessage> successQueuePublisher,
                                                             @Qualifier("failureQueuePublisher") QueuePublisher<NotificationQueueMessage> failureQueuePublisher,
                                                             @Qualifier("retryQueuePublisher") QueuePublisher<NotificationQueueMessage> retryQueuePublisher
    ) {
        return new NotificationSenderWorker(queue, executor, sseService, successQueuePublisher, failureQueuePublisher, retryQueuePublisher);
    }

    @Bean
    public NotificationOutboxCommitter notificationSuccessCommitter(@Qualifier("notificationSuccessExecutor") ThreadPoolTaskExecutor executor,
                                                                    NotifyOutboxCommitterProps props,
                                                                    @Qualifier("successQueue") BlockingQueue<NotificationQueueMessage> successQueue,
                                                                    NotificationOutboxService outboxService) {
        return new NotificationSuccessCommitter(executor, props, successQueue, outboxService);
    }

    @Bean
    public NotificationOutboxCommitter notificationFailureCommitter(@Qualifier("notificationFailureExecutor") ThreadPoolTaskExecutor executor,
                                                                    NotifyOutboxCommitterProps props,
                                                                    @Qualifier("failureQueue") BlockingQueue<NotificationQueueMessage> failureQueue,
                                                                    NotificationOutboxService outboxService) {
        return new NotificationFailureCommitter(executor, props, failureQueue, outboxService);
    }

    @Bean
    public NotificationOutboxCommitter notificationRetryCommitter(@Qualifier("notificationRetryExecutor") ThreadPoolTaskExecutor executor,
                                                                  NotifyOutboxCommitterProps props,
                                                                  @Qualifier("retryQueue") BlockingQueue<NotificationQueueMessage> retryQueue,
                                                                  NotificationOutboxService outboxService,
                                                                  BackoffCalculator backoffCalculator) {
        return new NotificationRetryCommitter(executor, props, retryQueue, outboxService, backoffCalculator);
    }

    @Bean
    public QueuePublisher<NotificationCreateQueueMessage> inviteNotificationQueuePublisher(@Qualifier("inviteNotificationQueue") BlockingQueue<NotificationCreateQueueMessage> queue) {
        return new InMemoryNotificationQueuePublisher<>(queue);
    }

    @Bean
    BlockingQueue<NotificationCreateQueueMessage> inviteNotificationQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public QueuePublisher<NotificationQueueMessage> notificationQueuePublisher(@Qualifier("notificationQueue") BlockingQueue<NotificationQueueMessage> queue) {
        return new InMemoryNotificationQueuePublisher<>(queue);
    }

    @Bean
    BlockingQueue<NotificationQueueMessage> notificationQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public QueuePublisher<NotificationQueueMessage> successQueuePublisher(@Qualifier("successQueue") BlockingQueue<NotificationQueueMessage> queue) {
        return new InMemoryNotificationQueuePublisher<>(queue);
    }

    @Bean
    BlockingQueue<NotificationQueueMessage> successQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public QueuePublisher<NotificationQueueMessage> failureQueuePublisher(@Qualifier("failureQueue") BlockingQueue<NotificationQueueMessage> queue) {
        return new InMemoryNotificationQueuePublisher<>(queue);
    }

    @Bean
    BlockingQueue<NotificationQueueMessage> failureQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public QueuePublisher<NotificationQueueMessage> retryQueuePublisher(@Qualifier("retryQueue") BlockingQueue<NotificationQueueMessage> queue) {
        return new InMemoryNotificationQueuePublisher<>(queue);
    }

    @Bean
    BlockingQueue<NotificationQueueMessage> retryQueue() {
        return new LinkedBlockingQueue<>(500);
    }

    @Bean
    public ThreadPoolTaskExecutor defaultAsyncExecutor(AsyncExecStats stats) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(32);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-");

        executor.setTaskDecorator(r -> {
            long submittedAt = System.nanoTime();
            return () -> {
                long start = System.nanoTime();
                long queueDelayMs = (start - submittedAt) / 1_000_000;
                try {
                    r.run();
                } finally {
                    long runTimeMs = (System.nanoTime() - start) / 1_000_000;

                    // 작업 시간 기록
                    stats.record(queueDelayMs, runTimeMs);
                }
            };
        });

        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationCreateExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("notify-create-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationClaimerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("notify-claimer-");
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
