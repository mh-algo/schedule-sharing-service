package com.minhyung.schedule.notification.domain;

public record NotificationRetryInfo(
        Long id,
        Long receiverId,
        String payload
) {
}
