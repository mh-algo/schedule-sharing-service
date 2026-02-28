package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.MessageType;
import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.event.InvitationCreatedEvent;
import com.minhyung.schedule.notification.event.NotificationEvent;
import com.minhyung.schedule.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Deprecated
@Slf4j
public class LegacyInviteNotificationOutboxEventHandler implements NotificationCreateEventHandler {
    private final NotificationService notificationService;
    private final PayloadEncoderRegistry payloadEncoderRegistry;

    public LegacyInviteNotificationOutboxEventHandler(NotificationService notificationService, PayloadEncoderRegistry payloadEncoderRegistry) {
        this.notificationService = notificationService;
        this.payloadEncoderRegistry = payloadEncoderRegistry;
    }

    private NotificationData toNotification(InvitationCreatedEvent event) {
        String payload = payloadEncoderRegistry.encode(event);

        return NotificationData.builder()
                .messageType(MessageType.GROUP_INVITE)
                .receiverId(event.inviteeId())
                .targetType(event.type())
                .targetId(event.id())
                .payload(payload)
                .build();
    }

    @Async("defaultAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void onEvent(NotificationEvent notificationEvent) {
        if (notificationEvent instanceof InvitationCreatedEvent event) {
            // 알림 및 알림 전송 정보 생성
            NotificationData data = toNotification(event);

            long start = System.nanoTime();

            notificationService.createNotification(data);

            long end = System.nanoTime();
            long taskMs = (end - start) / 1_000_000;
            if (taskMs >= 500) {
                log.warn("[INVITE NOTIFICATION] save={}ms", taskMs);
            }
        }
    }
}
