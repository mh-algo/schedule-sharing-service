package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.QueueFailed;
import com.minhyung.schedule.notification.domain.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class InMemoryNotificationQueuePublisher implements QueuePublisher {
    private final BlockingQueue<QueueMessage> notificationQueue;

    @Override
    public boolean publish(QueueMessage message) {
        boolean published = notificationQueue.offer(message);
        if (!published) {
            log.warn("notificationQueue is full");
        }
        return published;
    }

    @Override
    public List<QueueFailed> publishAll(List<QueueMessage> messages) {
        List<QueueFailed> failed = new ArrayList<>();
        for (QueueMessage message : messages) {
            boolean published = publish(message);

            // 큐 삽입 실패
            if (!published) {
                failed.add(QueueFailed.of(message.sendingId(), message.attempt()));
            }
        }
        return failed;
    }
}
