package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.MessageType;
import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.domain.NotificationSendingInfo;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.event.InvitationCreatedEvent;
import com.minhyung.schedule.notification.event.NotificationEvent;
import com.minhyung.schedule.notification.event.NotificationPreparedEvent;
import com.minhyung.schedule.notification.service.NotificationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

public class InviteNotificationCreateEventHandler implements NotificationCreateEventHandler {
    private final NotificationService notificationService;
    private final ApplicationEventPublisher publisher;
    private final PayloadEncoderRegistry payloadEncoderRegistry;

    public InviteNotificationCreateEventHandler(NotificationService notificationService,
                                                ApplicationEventPublisher publisher,
                                                PayloadEncoderRegistry payloadEncoderRegistry) {
        this.notificationService = notificationService;
        this.publisher = publisher;
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
            NotificationSendingInfo sendingInfo = notificationService.createNotification(data);

            // 전송할 알림 publish
            publisher.publishEvent(new NotificationPreparedEvent(sendingInfo.sendingId(), sendingInfo.receiverId(),
                    sendingInfo.payload(), sendingInfo.attempt()));
        }
    }
}
