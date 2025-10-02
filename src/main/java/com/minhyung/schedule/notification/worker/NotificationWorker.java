package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.QueueMessage;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class NotificationWorker {
    private final BlockingQueue<QueueMessage> queue;
    private final ThreadPoolTaskExecutor executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    protected NotificationWorker(BlockingQueue<QueueMessage> queue,
                              @Qualifier("notificationExecutor") ThreadPoolTaskExecutor executor) {
        this.queue = queue;
        this.executor = executor;
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
        if (running.compareAndSet(false, true)) {
            int n = Math.max(1, executor.getCorePoolSize());
            log.debug("ThreadPoolSize: {}", n);
            for (int i = 0; i < n; i++) {
                executor.submit(this::consume);
            }
        }
    }

    @PreDestroy
    protected void stop() {
        running.set(false);
        executor.shutdown();    // 스레드 풀 종료
    }

    protected void consume() {
        Thread currentThread = Thread.currentThread();
        while (running.get()) {
            try {
                QueueMessage message = queue.poll(300, TimeUnit.MILLISECONDS);
                if (!running.get()) break;  // 종료 신호 반영
                if (message == null) continue;    // 메시지 없으면 다음 루프
                // TODO: 알림 전송
                log.debug("{} - queue size: {}", currentThread.getName(), queue.size());
            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            } catch (Exception e) {
                // TODO: 재시도 로직
            }
        }
    }
}
