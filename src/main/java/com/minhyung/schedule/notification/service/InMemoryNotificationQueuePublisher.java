package com.minhyung.schedule.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class InMemoryNotificationQueuePublisher<T> implements QueuePublisher<T> {
    private final BlockingQueue<T> queue;

    @Override
    public boolean publish(T message) {
        boolean published = queue.offer(message);
        if (!published) {
            log.debug("Queue is full");
        }
        return published;
    }

    @Override
    public List<T> publishAll(List<T> messages) {
        List<T> failed = new ArrayList<>();
        for (T message : messages) {
            boolean published = publish(message);

            // 큐 삽입 실패
            if (!published) {
                failed.add(message);
            }
        }

        if (!failed.isEmpty()) {
            log.debug("Queue is full! failed count: {}", failed.size());
        }
        return failed;
    }
}
