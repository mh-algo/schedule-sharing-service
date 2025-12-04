package com.minhyung.schedule.notification.domain;

public record NotificationQueueMessage (
        long id,
        long receiverId,
        String payload,
        int attempt
) implements QueueMessage {
    public static NotificationQueueMessage of(long id, long receiverId, String payload, int attempt) {
        return new NotificationQueueMessage(id, receiverId, payload, attempt);
    }

    public static NotificationQueueMessage from(OutboxInfo outboxInfo) {
        return new NotificationQueueMessage(outboxInfo.id(), outboxInfo.receiverId(), outboxInfo.payload(), outboxInfo.attempt());
    }
}
