package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.MessageType;
import com.minhyung.schedule.notification.domain.NotificationCreateQueueMessage;
import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.event.InvitationCreatedEvent;
import com.minhyung.schedule.notification.event.NotificationEvent;
import com.minhyung.schedule.notification.service.QueuePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
public class InviteNotificationOutboxBatchEventHandler implements NotificationCreateEventHandler {
    private final QueuePublisher<NotificationCreateQueueMessage> inviteNotificationQueuePublisher;
    private final PayloadEncoderRegistry payloadEncoderRegistry;

    public InviteNotificationOutboxBatchEventHandler(QueuePublisher<NotificationCreateQueueMessage> inviteNotificationQueuePublisher, PayloadEncoderRegistry payloadEncoderRegistry) {
        this.inviteNotificationQueuePublisher = inviteNotificationQueuePublisher;
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

            // TODO: 배치 처리를 위해 큐에 publish
            inviteNotificationQueuePublisher.publish(NotificationCreateQueueMessage.from(data));

            long end = System.nanoTime();
            long taskMs = (end - start) / 1_000_000;
            if (taskMs >= 500) {
                log.warn("[INVITE NOTIFICATION] save={}ms", taskMs);
            }
        }
    }
}
