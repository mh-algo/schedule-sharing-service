package com.minhyung.schedule.notification.domain;

public record MessageEnvelope(
        String type,
        Object data
) {
}
