package com.minhyung.schedule.notification.domain;

public record RetryQueueMessage(
        long id,
        long receiverId,
        String payload,
        int attempt,
        String error
) implements QueueMessage  {
    public static RetryQueueMessage from(QueueMessage message, String error) {
        return new RetryQueueMessage(message.id(), message.receiverId(), message.payload(), message.attempt(), error);
    }
}
