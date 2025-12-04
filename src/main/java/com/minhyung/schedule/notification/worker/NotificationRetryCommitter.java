package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.domain.RetryQueueMessage;
import com.minhyung.schedule.notification.props.NotifyOutboxCommitterProps;
import com.minhyung.schedule.notification.service.BackoffCalculator;
import com.minhyung.schedule.notification.service.NotificationOutboxService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class NotificationRetryCommitter extends NotificationOutboxCommitter {
    private final BlockingQueue<QueueMessage> retryQueue;
    private final NotificationOutboxService outboxService;
    private final BackoffCalculator backoffCalculator;

    public NotificationRetryCommitter(ThreadPoolTaskExecutor executor,
                                      NotifyOutboxCommitterProps props,
                                      BlockingQueue<QueueMessage> retryQueue,
                                      NotificationOutboxService outboxService,
                                      BackoffCalculator backoffCalculator) {
        super(executor, props);
        this.retryQueue = retryQueue;
        this.outboxService = outboxService;
        this.backoffCalculator = backoffCalculator;
    }

    @Override
    protected boolean task(List<QueueMessage> buffer) {
        QueueMessage message = retryQueue.poll();
        if (message instanceof RetryQueueMessage retryMessage) {
            long backoff = backoffCalculator.calculate(retryMessage.attempt());
            LocalDateTime nextPushAt = LocalDateTime.now().plusSeconds(backoff);
            int updated = outboxService.updateRetryPending(retryMessage.id(), nextPushAt, retryMessage.error());
            return updated > 0;
        }
        return false;
    }
}
