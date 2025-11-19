package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.domain.NotificationSendingInfo;
import com.minhyung.schedule.notification.event.NotificationEvent;
import com.minhyung.schedule.notification.event.NotificationPreparedEvent;
import com.minhyung.schedule.notification.service.NotificationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public abstract class AbstractNotificationOutBoxEventHandler<E extends NotificationEvent> {
    private final NotificationService notificationService;
    private final ApplicationEventPublisher publisher;

    protected AbstractNotificationOutBoxEventHandler(NotificationService notificationService,
                                                     ApplicationEventPublisher publisher) {
        this.notificationService = notificationService;
        this.publisher = publisher;
    }

    protected abstract NotificationData toNotification(E event);

    @Async("defaultAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(E event) {
        // 알림 및 알림 전송 정보 생성
        NotificationData data = toNotification(event);
        NotificationSendingInfo sendingInfo = notificationService.createNotification(data);

        // 전송할 알림 publish
        publisher.publishEvent(new NotificationPreparedEvent(sendingInfo.sendingId(), sendingInfo.receiverId(),
                sendingInfo.payload(), sendingInfo.attempt()));
    }
}
