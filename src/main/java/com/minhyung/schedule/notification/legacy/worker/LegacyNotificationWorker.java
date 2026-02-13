package com.minhyung.schedule.notification.legacy.worker;

import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.domain.SendResult;
import com.minhyung.schedule.notification.service.BackoffCalculator;
import com.minhyung.schedule.notification.legacy.service.LegacyNotificationStatusService;
import com.minhyung.schedule.notification.service.SseService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Deprecated(forRemoval = true)
@Slf4j
public class LegacyNotificationWorker {
    private final BlockingQueue<QueueMessage> queue;
    private final ThreadPoolTaskExecutor executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SseService sseService;
    private final LegacyNotificationStatusService legacyNotificationStatusService;
    private final BackoffCalculator backoffCalculator;

    public LegacyNotificationWorker(BlockingQueue<QueueMessage> queue,
                                    ThreadPoolTaskExecutor executor,
                                    SseService sseService,
                                    LegacyNotificationStatusService legacyNotificationStatusService,
                                    BackoffCalculator backoffCalculator) {
        this.queue = queue;
        this.executor = executor;
        this.sseService = sseService;
        this.legacyNotificationStatusService = legacyNotificationStatusService;
        this.backoffCalculator = backoffCalculator;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void start() {
        if (running.compareAndSet(false, true)) {
            int n = Math.max(1, executor.getCorePoolSize());
            log.debug("ThreadPoolSize: {}", n);
            for (int i = 0; i < n; i++) {
                executor.submit(this::consume);
            }
        }
    }

    @PreDestroy
    private void stop() {
        running.set(false);
        executor.shutdown();    // 스레드 풀 종료
    }

    private void consume() {
        Thread currentThread = Thread.currentThread();
        while (running.get()) {
            try {
                long startTime = System.currentTimeMillis();
                QueueMessage message = queue.poll(300, TimeUnit.MILLISECONDS);
                if (!running.get()) break;  // 종료 신호 반영
                if (message == null) continue;    // 메시지 없으면 다음 루프
                sendNotification(message);
                log.debug("{} - queue size: {}", currentThread.getName(), queue.size());
                log.debug("send time: {}", System.currentTimeMillis() - startTime);
            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            }
        }
    }

    private void sendNotification(QueueMessage message) {
        long sendingId = message.id();
        int attempt = message.attempt();

        // sending 상태 progressing으로 변경
        boolean changed = legacyNotificationStatusService.changeProgressing(sendingId, attempt);
        if (!changed) {
            log.warn("skip sending: already claimed or not ready (id={})", sendingId);
            return;
        }

        // 알림 전송
        SendResult sendResult = sseService.sendNotification(message.receiverId(), sendingId, message.payload());

        // sending 상태 SENT로 변경
        if (sendResult.success()) {
            legacyNotificationStatusService.changeSent(sendingId);
        } else {
            String lastErr = sendResult.lastErr();
            if (lastErr != null) {  // sse 전송 도중 에러가 발생한 경우
                // 전송 상태를 RETRY_PENDING로 변경
                long backoff = backoffCalculator.calculate(attempt);
                legacyNotificationStatusService.changeRetryPending(sendingId, lastErr, backoff);
            } else {    // SseEmitter가 존재하지 않아서 전송 실패한 경우
                // 전송 상태를 FAILED로 변경
                legacyNotificationStatusService.changeFailed(sendingId);
            }
        }
    }
}
