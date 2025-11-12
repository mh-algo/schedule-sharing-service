package com.minhyung.schedule.notification.domain;

public record QueueMessage(
        long sendingId,
        long receiverId,
        String payload,
        int attempt
) {
    public static QueueMessage of(long sendingId, long receiverId, String payload, int attempt) {
        return new QueueMessage(sendingId, receiverId, payload, attempt);
    }
}
