package com.minhyung.schedule.notification.domain;

public record SendResult(
        boolean success,
        String lastErr
) {
}
