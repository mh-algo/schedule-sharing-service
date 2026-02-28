package com.minhyung.schedule.notification.legacy.domain;

@Deprecated(forRemoval = true)
public record RetryInfo(
        Long sendingId,
        Long notificationId,
        Integer attempt
) {
}
