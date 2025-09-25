package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryNotificationQueuePublisher implements QueuePublisher {
    private final BlockingQueue<QueueMessage> notificationQueue;

    @Override
    public boolean publish(QueueMessage message) {
        boolean isPublished = notificationQueue.offer(message);
        if (!isPublished) {
            log.warn("notificationQueue is full");
        }
        return isPublished;
    }
}
