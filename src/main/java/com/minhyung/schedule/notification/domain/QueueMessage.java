package com.minhyung.schedule.notification.domain;

public interface QueueMessage {
    long id();
    long receiverId();
    String payload();
    int attempt();
}
