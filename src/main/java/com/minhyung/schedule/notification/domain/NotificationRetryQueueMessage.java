package com.minhyung.schedule.notification.domain;

public record NotificationRetryQueueMessage(
        long id,
        long receiverId,
        String payload,
        int attempt,
        String error
) implements NotificationQueueMessage {
    public static NotificationRetryQueueMessage from(NotificationQueueMessage message, String error) {
        return new NotificationRetryQueueMessage(message.id(), message.receiverId(), message.payload(), message.attempt(), error);
    }
}
