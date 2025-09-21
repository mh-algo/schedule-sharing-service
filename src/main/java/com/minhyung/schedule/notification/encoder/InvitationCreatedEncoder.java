package com.minhyung.schedule.notification.encoder;

import com.minhyung.schedule.notification.domain.Inviter;
import com.minhyung.schedule.notification.domain.MessageEnvelope;
import com.minhyung.schedule.notification.event.InvitationCreatedEvent;
import lombok.AccessLevel;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InvitationCreatedEncoder implements PayloadEncoder<InvitationCreatedEvent> {
    @Override
    public MessageEnvelope toEnvelope(InvitationCreatedEvent event) {
        InvitationCreatedData data = InvitationCreatedData.builder()
                .inviteId(event.id())
                .groupId(event.groupId())
                .groupName(event.groupName())
                .inviter(event.inviter())
                .createdAt(event.createdAt())
                .expiresAt(event.expiresAt())
                .build();

        return new MessageEnvelope(event.type().name(), data);
    }

    @Override
    public Class<InvitationCreatedEvent> supports() {
        return InvitationCreatedEvent.class;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private record InvitationCreatedData(
            Long inviteId,
            Long groupId,
            String groupName,
            Inviter inviter,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ){
    }
}
