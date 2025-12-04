package com.minhyung.schedule.notification.domain;

public record OutboxInfo(long id,
                         long receiverId,
                         String payload,
                         int attempt
) {
    public static OutboxInfo of(long id, long receiverId, String payload, int attempt) {
        return new OutboxInfo(id, receiverId, payload, attempt);
    }

    public static OutboxInfo from(QueueMessage message) {
        return new OutboxInfo(message.id(), message.receiverId(), message.payload(), message.attempt());
    }
}
