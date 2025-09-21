package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.MessageType;
import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.event.InvitationCreatedEvent;
import com.minhyung.schedule.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InviteEventHandler {
    private final PayloadEncoderRegistry payloadEncoderRegistry;
    private final NotificationService notificationService;

    public InviteEventHandler(PayloadEncoderRegistry payloadEncoderRegistry,
                              @Qualifier("invitationNotificationService") NotificationService notificationService) {
        this.payloadEncoderRegistry = payloadEncoderRegistry;
        this.notificationService = notificationService;
    }

    @EventListener
    @Transactional
    public void inviteCreated(InvitationCreatedEvent event) {
        String payload = payloadEncoderRegistry.encode(event);

        NotificationData data = NotificationData.builder()
                .messageType(MessageType.GROUP_INVITE)
                .receiverId(event.inviteeId())
                .targetType(event.type())
                .targetId(event.id())
                .payload(payload)
                .build();
        notificationService.create(data);
    }
}
