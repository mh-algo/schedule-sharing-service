package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.QueueMessage;

public interface QueuePublisher {
    boolean publish(QueueMessage message);
}
