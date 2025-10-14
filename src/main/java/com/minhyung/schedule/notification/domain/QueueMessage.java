package com.minhyung.schedule.notification.domain;

public record QueueMessage(
        long sendingId,
        long receiverId,
        String payload
) {
}
