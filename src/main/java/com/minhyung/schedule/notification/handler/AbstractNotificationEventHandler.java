package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.domain.NotificationSendingInfo;
import com.minhyung.schedule.notification.event.NotificationEvent;
import com.minhyung.schedule.notification.event.NotificationPreparedEvent;
import com.minhyung.schedule.notification.service.NotificationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public abstract class AbstractNotificationEventHandler<E extends NotificationEvent> {
    private final NotificationService notificationService;
    private final ApplicationEventPublisher publisher;

    protected AbstractNotificationEventHandler(NotificationService notificationService,
                                               ApplicationEventPublisher publisher) {
        this.notificationService = notificationService;
        this.publisher = publisher;
    }

    protected abstract NotificationData toNotification(E event);

    @Transactional
    @EventListener
    public void onEvent(E event) {
        // 알림 및 알림 전송 정보 생성
        NotificationData data = toNotification(event);
        NotificationSendingInfo sendingInfo = notificationService.createNotification(data);

        // 전송할 알림 publish
        publisher.publishEvent(new NotificationPreparedEvent(sendingInfo.sendingId(), sendingInfo.receiverId(), sendingInfo.payload()));
    }
}
