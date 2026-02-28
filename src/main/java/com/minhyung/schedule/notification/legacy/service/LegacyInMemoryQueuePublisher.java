package com.minhyung.schedule.notification.legacy.service;

import com.minhyung.schedule.notification.domain.NotificationQueueMessage;
import com.minhyung.schedule.notification.domain.QueueFailed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Deprecated(forRemoval = true)
@Slf4j
@RequiredArgsConstructor
public class LegacyInMemoryQueuePublisher implements LegacyQueuePublisher {
    private final BlockingQueue<NotificationQueueMessage> queue;

    @Override
    public boolean publish(NotificationQueueMessage message) {
        boolean published = queue.offer(message);
        if (!published) {
            log.debug("Queue is full");
        }
        return published;
    }

    @Override
    public List<QueueFailed> publishAll(List<NotificationQueueMessage> messages) {
        List<QueueFailed> failed = new ArrayList<>();
        for (NotificationQueueMessage message : messages) {
            boolean published = publish(message);

            // 큐 삽입 실패
            if (!published) {
                failed.add(QueueFailed.of(message.id(), message.attempt()));
            }
        }

        if (!failed.isEmpty()) {
            log.debug("Queue is full! failed count: {}", failed.size());
        }
        return failed;
    }
}
