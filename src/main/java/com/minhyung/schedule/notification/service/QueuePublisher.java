package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.QueueFailed;
import com.minhyung.schedule.notification.domain.QueueMessage;

import java.util.List;

public interface QueuePublisher {
    boolean publish(QueueMessage message);
    List<QueueFailed> publishAll(List<QueueMessage> messages);
}
