package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.OutboxInfo;
import com.minhyung.schedule.notification.domain.NotificationClaimQueueMessage;
import com.minhyung.schedule.notification.domain.NotificationQueueMessage;
import com.minhyung.schedule.notification.props.NotifyOutboxClaimerProps;
import com.minhyung.schedule.notification.service.NotificationOutboxService;
import com.minhyung.schedule.notification.service.QueuePublisher;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NotificationClaimer {
    private final QueuePublisher<NotificationQueueMessage> queuePublisher;
    private final ThreadPoolTaskExecutor executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final NotifyOutboxClaimerProps props;
    private final NotificationOutboxService outboxService;

    public NotificationClaimer(QueuePublisher<NotificationQueueMessage> queuePublisher, ThreadPoolTaskExecutor executor, NotifyOutboxClaimerProps props, NotificationOutboxService outboxService) {
        this.queuePublisher = queuePublisher;
        this.executor = executor;
        this.props = props;
        this.outboxService = outboxService;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void start() {
        if (running.compareAndSet(false, true)) {
            int n = Math.max(1, executor.getCorePoolSize());
            log.debug("ThreadPoolSize: {}", n);
            for (int i = 0; i < n; i++) {
                executor.submit(this::task);
            }
        }
    }

    @PreDestroy
    private void stop() {
        running.set(false);
        executor.shutdown();    // 스레드 풀 종료
    }

    private void task() {
        int count = 0;
        Thread currentThread = Thread.currentThread();
        while (running.get()) {
            try {
                List<OutboxInfo> outboxInfoList = outboxService.claimBatch(props.batchSize());

                if (outboxInfoList.isEmpty()) {
                    long backoff = Math.min(props.baseIntervalMs() * ++count, props.maxIntervalMs());
                    Thread.sleep(backoff);
                    continue;
                }

                count = 0;
                List<NotificationQueueMessage> queueMessages = outboxInfoList.stream()
                        .map(message -> (NotificationQueueMessage) NotificationClaimQueueMessage.from(message))
                        .toList();

                queuePublisher.publishAll(queueMessages);

            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            }
        }
    }
}
