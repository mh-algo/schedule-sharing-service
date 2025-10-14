package com.minhyung.schedule.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    SseEmitter save(long userId, long emitterId, SseEmitter emitter);
    Map<Long, SseEmitter> findAllByUserId(long userId);
    void delete(long userId, long emitterId);
    void cacheEvent(long userId, long eventId, Object data);
    Map<Long, Object> getCachedEventsAfter(long userId, long lastEventId);
}
