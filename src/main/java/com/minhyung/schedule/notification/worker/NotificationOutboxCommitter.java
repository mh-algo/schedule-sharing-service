package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.props.NotifyOutboxCommitterProps;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class NotificationOutboxCommitter {
    private final ThreadPoolTaskExecutor executor;
    private final NotifyOutboxCommitterProps props;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<QueueMessage> buffer = new ArrayList<>();
    private int attempts = 0;

    protected NotificationOutboxCommitter(ThreadPoolTaskExecutor executor, NotifyOutboxCommitterProps props) {
        this.executor = executor;
        this.props = props;
    }

    protected final NotifyOutboxCommitterProps props() {
        return props;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void start() {
        if (running.compareAndSet(false, true)) {
            int n = Math.max(1, executor.getCorePoolSize());
            log.debug("ThreadPoolSize: {}", n);
            for (int i = 0; i < n; i++) {
                executor.submit(this::run);
            }
        }
    }

    @PreDestroy
    private void stop() {
        running.set(false);
        executor.shutdown();    // 스레드 풀 종료
    }

    private void run() {
        Thread currentThread = Thread.currentThread();
        while (running.get()) {
            try {
                buffer.clear();
                boolean worked = task(buffer);

                // task가 실행된 경우 시도 횟수 초기화
                if (worked) {
                    attempts = 0;
                } else {
                    long backoff = calculateBackoff(++attempts);
                    Thread.sleep(backoff);
                }
            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            }
        }
    }

    protected abstract boolean task(List<QueueMessage> buffer);

    protected long calculateBackoff(int attempts) {
        return Math.min(props.baseIntervalMs() * attempts, props.maxIntervalMs());
    }
}
