package com.minhyung.schedule.notification.domain;

import lombok.Builder;

@Builder
public record NotificationData(
        MessageType messageType,
        Long receiverId,
        NotificationType targetType,
        Long targetId,
        String payload) {
}
