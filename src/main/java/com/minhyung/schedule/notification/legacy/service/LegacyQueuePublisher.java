package com.minhyung.schedule.notification.legacy.service;

import com.minhyung.schedule.notification.domain.NotificationQueueMessage;
import com.minhyung.schedule.notification.domain.QueueFailed;

import java.util.List;

public interface LegacyQueuePublisher {
    boolean publish(NotificationQueueMessage message);
    List<QueueFailed> publishAll(List<NotificationQueueMessage> messages);
}
