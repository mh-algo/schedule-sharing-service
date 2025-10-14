package com.minhyung.schedule.notification.domain;

public record NotificationSendingInfo(
        Long sendingId,
        Long receiverId,
        String payload
) {
}
