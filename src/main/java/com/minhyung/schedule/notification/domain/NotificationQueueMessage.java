package com.minhyung.schedule.notification.domain;

public interface NotificationQueueMessage {
    long id();
    long receiverId();
    String payload();
    int attempt();
}
