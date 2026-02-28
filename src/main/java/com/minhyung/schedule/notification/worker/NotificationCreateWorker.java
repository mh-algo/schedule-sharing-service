package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.NotificationCreateQueueMessage;
import com.minhyung.schedule.notification.props.NotifyOutboxCreateProps;
import com.minhyung.schedule.notification.repository.NotificationOutboxJdbcRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class NotificationCreateWorker {
    private final BlockingQueue<NotificationCreateQueueMessage> notificationQueue;
    private final ThreadPoolTaskExecutor executor;
    private final NotifyOutboxCreateProps props;
    private final NotificationOutboxJdbcRepository notificationOutboxJdbcRepository;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<NotificationCreateQueueMessage> buffer = new ArrayList<>();
    private int attempts = 0;

    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
        if (running.compareAndSet(false, true)) {
            int n = Math.max(1, executor.getCorePoolSize());
            log.debug("ThreadPoolSize: {}", n);
            for (int i = 0; i < n; i++) {
                executor.submit(this::task);
            }
        }
    }

    @PreDestroy
    protected void stop() {
        running.set(false);
        executor.shutdown();    // 스레드 풀 종료
    }

    private void task() {
        Thread currentThread = Thread.currentThread();
        while (running.get()) {
            try {
                buffer.clear();
                int count = notificationQueue.drainTo(buffer, props.batchSize());

                if (!running.get()) break;  // 종료 신호 반영

                // 큐에 데이터가 존재하는 경우 batch insert
                if (count > 0) {
                    attempts = 0;

                    // batch insert
                    notificationOutboxJdbcRepository.batchInsert(buffer);
                }
                // 큐에 데이터가 존재하지 않는 경우 backoff 계산 후 sleep
                else {
                    long backoff = calculateBackoff(++attempts);
                    Thread.sleep(backoff);
                }
            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            }
        }
    }

    protected long calculateBackoff(int attempts) {
        return Math.min(props.baseIntervalMs() * attempts, props.maxIntervalMs());
    }
}
