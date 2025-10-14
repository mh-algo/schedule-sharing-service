package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.event.NotificationPreparedEvent;
import com.minhyung.schedule.notification.service.NotificationStatusService;
import com.minhyung.schedule.notification.service.QueuePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationPreparedEventHandler {
    private final QueuePublisher queuePublisher;
    private final NotificationStatusService notificationStatusService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(NotificationPreparedEvent event) {
        Long sendingId = event.sendingId();

        // sending 상태 READY로 변경
        boolean changed = notificationStatusService.changeReady(sendingId);
        boolean published = false;

        if (changed) {
            // 전송할 알림 큐에 삽입
            published = queuePublisher.publish(new QueueMessage(sendingId, event.receiverId(), event.payload()));
        }

        // sending 상태 변경 또는 큐 삽입 실패
        if (!changed || !published) {
            // TODO: 나중에 재시도 하도록 시도 횟수 + 1, next_push_at 설정
        }
    }
}
