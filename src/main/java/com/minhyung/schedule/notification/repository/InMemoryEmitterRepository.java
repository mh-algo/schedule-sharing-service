package com.minhyung.schedule.notification.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class InMemoryEmitterRepository implements EmitterRepository {
    // 유저별 다중 emitter
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<Long, SseEmitter>> emitters = new ConcurrentHashMap<>();
    // 유저별 이벤트 캐시(정렬 보장)
    private static final ConcurrentHashMap<Long, ConcurrentSkipListMap<Long, Object>> eventCache = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(long userId, long emitterId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(emitterId, emitter);
        return emitter;
    }

    @Override
    public void delete(long userId, long emitterId) {
        ConcurrentHashMap<Long, SseEmitter> map = emitters.get(userId);
        if (map != null) {
            map.remove(emitterId);
            if (map.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    @Override
    public Map<Long, Object> getCachedEventsAfter(long userId, long lastEventId) {
        ConcurrentSkipListMap<Long, Object> cache = eventCache.getOrDefault(userId, new ConcurrentSkipListMap<>());
        return cache.tailMap(lastEventId, false);
    }
}
