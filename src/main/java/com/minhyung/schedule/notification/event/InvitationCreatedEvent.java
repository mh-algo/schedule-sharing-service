package com.minhyung.schedule.notification.event;

import com.minhyung.schedule.notification.domain.Inviter;
import com.minhyung.schedule.notification.domain.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InvitationCreatedEvent (
        NotificationType type,
        Long id,
        Long groupId,
        String groupName,
        Inviter inviter,
        Long inviteeId,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) implements NotificationEvent {
}
