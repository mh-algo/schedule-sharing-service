package com.minhyung.schedule.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    SseEmitter save(long userId, long emitterId, SseEmitter emitter);
    void delete(long userId, long emitterId);
    Map<Long, Object> getCachedEventsAfter(long userId, long lastEventId);
}
