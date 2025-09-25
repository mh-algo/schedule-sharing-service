package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.QueueMessage;
import com.minhyung.schedule.notification.event.NotificationPreparedEvent;
import com.minhyung.schedule.notification.service.NotificationStatusService;
import com.minhyung.schedule.notification.service.QueuePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationPreparedEventHandler {
    private final QueuePublisher queuePublisher;
    private final NotificationStatusService notificationStatusService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(NotificationPreparedEvent event) {
        // 전송할 알림 큐에 삽입
        Long id = event.sendingId();
        boolean isPublished = queuePublisher.publish(new QueueMessage(id));
        if (isPublished) {  // 큐 삽입 성공
            // sending 상태 READY로 변경
            notificationStatusService.changeToReady(id);
        } else {    // 큐 삽입 실패
            // TODO: 나중에 재시도 하도록 시도 횟수 + 1, next_push_at 설정
        }
    }
}
