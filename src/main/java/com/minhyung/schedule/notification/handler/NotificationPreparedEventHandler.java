package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.event.NotificationPreparedEvent;
import com.minhyung.schedule.notification.service.BackoffCalculator;
import com.minhyung.schedule.notification.service.NotificationStatusService;
import com.minhyung.schedule.notification.service.QueuePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPreparedEventHandler {
    private final QueuePublisher queuePublisher;
    private final NotificationStatusService notificationStatusService;
    private final BackoffCalculator backoffCalculator;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(NotificationPreparedEvent event) {
        Long sendingId = event.sendingId();

        // sending 상태 READY로 변경
        boolean changed = notificationStatusService.changeReady(sendingId);
        boolean published = false;
        Integer attempt = event.attempt();

        if (changed) {
            // 전송할 알림 큐에 삽입
            published = queuePublisher.publish(
                    QueueMessage.of(sendingId, event.receiverId(), event.payload(), attempt));
        }

        // sending 상태 변경 또는 큐 삽입 실패
        if (!changed || !published) {
            String errorMessage = String.format("Sending status to READY: %s, Queue published: %s", changed, published);

            // 지수백오프 계산
            long backoff = backoffCalculator.calculate(attempt);

            // 나중에 재시도 할 수 있도록 sending 상태를 RETRY_PENDING로 변경
            boolean changedRetry = notificationStatusService.changeRetryPending(sendingId, errorMessage, backoff, attempt);
            if (!changedRetry) {
                log.warn("failed to change RETRY_PENDING: {}", sendingId);
            }
        }
    }
}
