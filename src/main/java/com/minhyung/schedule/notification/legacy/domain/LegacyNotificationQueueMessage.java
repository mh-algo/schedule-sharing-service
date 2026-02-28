package com.minhyung.schedule.notification.legacy.domain;

import com.minhyung.schedule.notification.domain.OutboxInfo;
import com.minhyung.schedule.notification.domain.NotificationQueueMessage;

@Deprecated(forRemoval = true)
public record LegacyNotificationQueueMessage(
        long id,
        long receiverId,
        String payload,
        int attempt
) implements NotificationQueueMessage {
    public static LegacyNotificationQueueMessage of(long id, long receiverId, String payload, int attempt) {
        return new LegacyNotificationQueueMessage(id, receiverId, payload, attempt);
    }

    public static LegacyNotificationQueueMessage from(OutboxInfo outboxInfo) {
        return new LegacyNotificationQueueMessage(outboxInfo.id(), outboxInfo.receiverId(), outboxInfo.payload(), outboxInfo.attempt());
    }
}
