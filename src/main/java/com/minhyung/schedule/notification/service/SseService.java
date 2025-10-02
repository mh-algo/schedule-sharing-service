package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {
    private final EmitterRepository emitterRepository;
    private final Clock clock;

    @Value("${sse.ttl-ms}")
    private Long ttl;

    @PreAuthorize("isAuthenticated() and authentication.principal.id == #userId")
    public SseEmitter connect(Long userId, Long lastEventId) {
        long emitterId = clock.millis();
        Runnable cleanup = () -> emitterRepository.delete(userId, emitterId);
        SseEmitter emitter = createAndSaveEmitter(userId, emitterId, cleanup);

        // 네트워크 유휴타임아웃(503 error) 방지
        send(emitter, emitterId, "heartbeat", "connected", cleanup);

        // 클라이언트의 Last-Event-ID 헤더를 받아서 누락 이벤트만 보내기
        if (lastEventId != null) {
            Map<Long, Object> missed = emitterRepository.getCachedEventsAfter(userId, lastEventId);
            missed.forEach((eventId, payload) -> send(emitter, eventId, payload, cleanup));
        }
        return emitter;
    }

    private SseEmitter createAndSaveEmitter(Long userId, Long emitterId, Runnable cleanup) {
        SseEmitter emitter = emitterRepository.save(userId, emitterId, new SseEmitter(ttl));
        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            emitter.complete();
            cleanup.run();
        });
        return emitter;
    }

    private void send(SseEmitter emitter, long eventId, Object data, Runnable cleanup) {
        send(emitter, eventId, "message", data, cleanup);
    }

    private void send(SseEmitter emitter, long eventId, String eventName, Object data, Runnable cleanup) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(eventId))
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.warn("Failed to send event", e);
            emitter.complete();
            cleanup.run();
        }
    }
}
