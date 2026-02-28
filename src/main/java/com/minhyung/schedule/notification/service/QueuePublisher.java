package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.QueueFailed;

import java.util.List;

public interface QueuePublisher<T> {
    boolean publish(T message);
    List<T> publishAll(List<T> messages);
}
