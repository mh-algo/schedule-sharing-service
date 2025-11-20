package com.minhyung.schedule.notification.config;

import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.handler.InviteNotificationOutboxEventHandler;
import com.minhyung.schedule.notification.handler.NotificationCreateEventHandler;
import com.minhyung.schedule.notification.repository.NotificationMessageRepository;
import com.minhyung.schedule.notification.repository.NotificationOutboxRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConditionalOnProperty(name = "notify.mode", havingValue="outbox", matchIfMissing=true)
public class NotificationConfig {
    @Bean
    public NotificationCreateEventHandler notificationCreateEventHandler(@Qualifier("invitationNotificationOutboxService") NotificationService notificationService,
                                                                         PayloadEncoderRegistry payloadEncoderRegistry) {
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
    public ThreadPoolTaskExecutor defaultAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
