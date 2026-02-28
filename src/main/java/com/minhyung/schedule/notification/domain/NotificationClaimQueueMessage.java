package com.minhyung.schedule.notification.domain;

public record NotificationClaimQueueMessage(
        long id,
        long receiverId,
        String payload,
        int attempt
) implements NotificationQueueMessage {
    public static NotificationClaimQueueMessage of(long id, long receiverId, String payload, int attempt) {
        return new NotificationClaimQueueMessage(id, receiverId, payload, attempt);
    }

    public static NotificationClaimQueueMessage from(OutboxInfo outboxInfo) {
        return new NotificationClaimQueueMessage(outboxInfo.id(), outboxInfo.receiverId(), outboxInfo.payload(), outboxInfo.attempt());
    }
}
