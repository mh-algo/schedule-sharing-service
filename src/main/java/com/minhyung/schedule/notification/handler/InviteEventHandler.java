package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.MessageType;
import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.encoder.PayloadEncoderRegistry;
import com.minhyung.schedule.notification.event.InvitationCreatedEvent;
import com.minhyung.schedule.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InviteEventHandler extends AbstractNotificationEventHandler<InvitationCreatedEvent> {
    private final PayloadEncoderRegistry payloadEncoderRegistry;

    public InviteEventHandler(@Qualifier("invitationNotificationService") NotificationService notificationService,
                              ApplicationEventPublisher publisher,
                              PayloadEncoderRegistry payloadEncoderRegistry) {
        super(notificationService, publisher);
        this.payloadEncoderRegistry = payloadEncoderRegistry;
    }

    @Override
    protected NotificationData toNotification(InvitationCreatedEvent event) {
        String payload = payloadEncoderRegistry.encode(event);

        return NotificationData.builder()
                .messageType(MessageType.GROUP_INVITE)
                .receiverId(event.inviteeId())
                .targetType(event.type())
                .targetId(event.id())
                .payload(payload)
                .build();
    }
}
