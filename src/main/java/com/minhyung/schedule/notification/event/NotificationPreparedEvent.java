package com.minhyung.schedule.notification.event;

public record NotificationPreparedEvent(
        Long sendingId,
        Long receiverId,
        String payload,
        Integer attempt
) {
}
