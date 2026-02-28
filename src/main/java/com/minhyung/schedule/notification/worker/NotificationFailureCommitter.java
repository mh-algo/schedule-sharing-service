package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.NotificationQueueMessage;
import com.minhyung.schedule.notification.props.NotifyOutboxCommitterProps;
import com.minhyung.schedule.notification.service.NotificationOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class NotificationFailureCommitter extends NotificationOutboxCommitter {
    private final BlockingQueue<NotificationQueueMessage> failureQueue;
    private final NotificationOutboxService outboxService;

    public NotificationFailureCommitter(ThreadPoolTaskExecutor executor,
                                        NotifyOutboxCommitterProps props,
                                        BlockingQueue<NotificationQueueMessage> failureQueue,
                                        NotificationOutboxService outboxService) {
        super(executor, props);
        this.failureQueue = failureQueue;
        this.outboxService = outboxService;
    }

    @Override
    public boolean task(List<NotificationQueueMessage> buffer) {
        int count = failureQueue.drainTo(buffer, props().batchSize());
        if (count <= 0) {
            return false;
        }

        List<Long> ids = buffer.stream()
                .map(NotificationQueueMessage::id)
                .toList();
        int deleted = outboxService.deleteAll(ids);
         log.debug("Deleted {} outbox rows", deleted);
        return deleted > 0;
    }
}
