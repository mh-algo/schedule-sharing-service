package com.minhyung.schedule.notification.domain;

public record QueueFailed(
        long sendingId,
        int attempt
) {
    public static QueueFailed of(long sendingId, int attempt) {
        return new QueueFailed(sendingId, attempt);
    }
}
