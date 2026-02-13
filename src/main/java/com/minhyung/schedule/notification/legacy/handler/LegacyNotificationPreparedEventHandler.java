package com.minhyung.schedule.notification.legacy.handler;

import com.minhyung.schedule.notification.domain.NotificationQueueMessage;
import com.minhyung.schedule.notification.event.NotificationPreparedEvent;
import com.minhyung.schedule.notification.service.BackoffCalculator;
import com.minhyung.schedule.notification.legacy.service.LegacyNotificationStatusService;
import com.minhyung.schedule.notification.service.QueuePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

@Deprecated(forRemoval = true)
@Slf4j
@RequiredArgsConstructor
public class LegacyNotificationPreparedEventHandler {
    private final QueuePublisher queuePublisher;
    private final LegacyNotificationStatusService legacyNotificationStatusService;
    private final BackoffCalculator backoffCalculator;

    @EventListener
    public void onEvent(NotificationPreparedEvent event) {
        Long sendingId = event.sendingId();

        Integer attempt = event.attempt();
        boolean published = queuePublisher.publish(NotificationQueueMessage.of(sendingId, event.receiverId(), event.payload(), attempt));

        // sending 상태 변경 또는 큐 삽입 실패
        if (!published) {
            String errorMessage = String.format("Queue published: %s", published);

            // 지수백오프 계산
            long backoff = backoffCalculator.calculate(attempt);

            // 나중에 재시도 할 수 있도록 sending 상태를 RETRY_PENDING로 변경
            boolean changedRetry = legacyNotificationStatusService.changeRetryPending(sendingId, errorMessage, backoff, attempt);
            if (!changedRetry) {
                log.warn("failed to change RETRY_PENDING: {}", sendingId);
            }
        }
    }
}
