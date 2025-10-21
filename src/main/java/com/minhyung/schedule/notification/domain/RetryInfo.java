package com.minhyung.schedule.notification.domain;

public record RetryInfo(
        Long sendingId,
        Long notificationId,
        Integer attempt
) {
}
