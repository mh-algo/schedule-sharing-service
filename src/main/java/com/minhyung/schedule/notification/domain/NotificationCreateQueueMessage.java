package com.minhyung.schedule.notification.domain;

import lombok.Builder;

@Builder
public record NotificationCreateQueueMessage(
        MessageType messageType,
        Long receiverId,
        NotificationType targetType,
        Long targetId,
        String payload
) {
    public static NotificationCreateQueueMessage from(NotificationData data) {
        return NotificationCreateQueueMessage.builder()
                .messageType(MessageType.GROUP_INVITE)
                .receiverId(data.receiverId())
                .targetType(data.targetType())
                .targetId(data.targetId())
                .payload(data.payload())
                .build();
    }
}
