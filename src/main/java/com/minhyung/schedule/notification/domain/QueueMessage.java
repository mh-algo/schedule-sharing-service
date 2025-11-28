package com.minhyung.schedule.notification.domain;

public record QueueMessage(
        long id,
        long receiverId,
        String payload,
        int attempt
) {
    public static QueueMessage of(long id, long receiverId, String payload, int attempt) {
        return new QueueMessage(id, receiverId, payload, attempt);
    }

    public static QueueMessage from(OutboxInfo outboxInfo) {
        return new QueueMessage(outboxInfo.id(), outboxInfo.receiverId(), outboxInfo.payload(), outboxInfo.attempt());
    }
}
