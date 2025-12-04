package com.minhyung.schedule.notification.worker;

import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.props.NotifyOutboxCommitterProps;
import com.minhyung.schedule.notification.service.NotificationOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class NotificationSuccessCommitter extends NotificationOutboxCommitter {
    private final BlockingQueue<QueueMessage> successQueue;
    private final NotificationOutboxService outboxService;

    public NotificationSuccessCommitter(ThreadPoolTaskExecutor executor,
                                        NotifyOutboxCommitterProps props,
                                        BlockingQueue<QueueMessage> successQueue,
                                        NotificationOutboxService outboxService) {
        super(executor, props);
        this.successQueue = successQueue;
        this.outboxService = outboxService;
    }

    @Override
    public boolean task(List<QueueMessage> buffer) {
        int count = successQueue.drainTo(buffer, props().batchSize());
        if (count <= 0) {
            return false;
        }

        List<Long> ids = buffer.stream()
                .map(QueueMessage::id)
                .toList();
        int deleted = outboxService.deleteAll(ids);
        log.debug("Deleted {} outbox rows", deleted);
        return deleted > 0;
    }
}
