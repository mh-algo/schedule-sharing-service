package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.SendResult;
import com.minhyung.schedule.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {
    private final EmitterRepository emitterRepository;
    private final Clock clock;

    @Value("${sse.emitters.ttl-ms}")
    private Long ttl;

    @PreAuthorize("isAuthenticated() and authentication.principal.id == #userId")
    public SseEmitter connect(Long userId, Long lastEventId) {
        long emitterId = clock.millis();
        Runnable cleanup = getCleanup(userId, emitterId);
        SseEmitter emitter = createAndSaveEmitter(userId, emitterId, cleanup);

        try {
            // 네트워크 유휴타임아웃(503 error) 방지
            send(emitter, emitterId, "heartbeat", "connected", cleanup);

            // 클라이언트의 Last-Event-ID 헤더를 받아서 누락 이벤트만 보내기
            if (lastEventId != null) {
                Map<Long, Object> missed = emitterRepository.getCachedEventsAfter(userId, lastEventId);
                for (Map.Entry<Long, Object> entry : new ArrayList<>(missed.entrySet())) {
                    Long eventId = entry.getKey();
                    Object data = entry.getValue();
                    send(emitter, eventId, data, cleanup);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to send event (receiverId={}, emitterId={}): {}", userId, emitterId, e.toString());
        }
        return emitter;
    }

    private Runnable getCleanup(Long userId, long emitterId) {
        return () -> emitterRepository.delete(userId, emitterId);
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

    private void send(SseEmitter emitter, long eventId, Object data, Runnable cleanup) throws IOException {
        send(emitter, eventId, "message", data, cleanup);
    }

    private void send(SseEmitter emitter, long eventId, String eventName, Object data, Runnable cleanup) throws IOException {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(eventId))
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.warn("Failed to send event", e);
            emitter.complete();
            cleanup.run();
            throw e;
        }
    }

    public SendResult sendNotification(long receiverId, long sendingId, Object data) {
        emitterRepository.cacheEvent(receiverId, sendingId, data);
        Map<Long, SseEmitter> emitters = emitterRepository.findAllByUserId(receiverId);
        log.info("emitters size = {}", emitters.size());

        int success = 0;
        String lastErr = null;
        // receiverId에 해당하는 모든 SseEmitter를 사용하여 알림 전송
        for (Map.Entry<Long, SseEmitter> entry : new ArrayList<>(emitters.entrySet())) {
            Long emitterId = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                log.info("Send Notification (receiverId: {}, sendingId: {})", receiverId, emitterId);
                send(emitter, sendingId, data, getCleanup(receiverId, emitterId));
                success++;
            } catch (IOException e) {
                log.warn("Failed to send event (receiverId={}, emitterId={}, sendingId={}): {}",
                        receiverId, emitterId, sendingId, e.toString());
                lastErr = e.toString();
            }
        }
        return new SendResult(success > 0, lastErr);
    }
}
