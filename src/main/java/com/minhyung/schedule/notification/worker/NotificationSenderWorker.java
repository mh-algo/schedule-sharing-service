package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.NotificationQueueMessage;
import com.minhyung.schedule.notification.domain.NotificationRetryQueueMessage;
import com.minhyung.schedule.notification.domain.SendResult;
import com.minhyung.schedule.notification.service.QueuePublisher;
import com.minhyung.schedule.notification.service.SseService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class NotificationSenderWorker {
    private final BlockingQueue<NotificationQueueMessage> queue;
    private final ThreadPoolTaskExecutor executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SseService sseService;
    private final QueuePublisher<NotificationQueueMessage> successQueuePublisher;
    private final QueuePublisher<NotificationQueueMessage> failureQueuePublisher;
    private final QueuePublisher<NotificationQueueMessage> retryQueuePublisher;

    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
        if (running.compareAndSet(false, true)) {
            int n = Math.max(1, executor.getCorePoolSize());
            log.debug("ThreadPoolSize: {}", n);
            for (int i = 0; i < n; i++) {
                executor.submit(this::sender);
            }
        }
    }

    @PreDestroy
    protected void stop() {
        running.set(false);
        executor.shutdown();    // 스레드 풀 종료
    }

    private void sender() {
        Thread currentThread = Thread.currentThread();
        while (running.get()) {
            try {
                NotificationQueueMessage message = queue.poll(300, TimeUnit.MILLISECONDS);
                if (!running.get()) break;  // 종료 신호 반영
                if (message == null) continue;    // 메시지 없으면 다음 루프
                sendNotification(message);  // sse 전송
            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            }
        }
    }

    private void sendNotification(NotificationQueueMessage message) {
        // sse 전송
        SendResult sendResult = sseService.sendNotification(message.receiverId(), message.id(), message.payload());

        if (sendResult.success()) {
            successQueuePublisher.publish(message);
        } else {
            String lastErr = sendResult.lastErr();
            if (lastErr != null) {  // sse 전송 도중 에러가 발생한 경우
                // 기존 객체에 lastErr 추가
                NotificationRetryQueueMessage retryMessage = NotificationRetryQueueMessage.from(message, lastErr);
                retryQueuePublisher.publish(retryMessage);
            } else {    // SseEmitter가 존재하지 않아서 전송 실패한 경우
                failureQueuePublisher.publish(message);
            }
        }
    }
}
