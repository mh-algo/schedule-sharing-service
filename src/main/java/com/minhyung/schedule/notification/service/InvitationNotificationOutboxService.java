package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.domain.NotificationSendingInfo;
import com.minhyung.schedule.notification.domain.SendingStatus;
import com.minhyung.schedule.notification.domain.entity.NotificationEntity;
import com.minhyung.schedule.notification.domain.entity.NotificationOutboxEntity;
import com.minhyung.schedule.notification.repository.NotificationOutboxRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class InvitationNotificationOutboxService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository outboxRepository;
    private final EntityManager em;

    @Transactional
    @Override
    public NotificationSendingInfo createNotification(NotificationData data) {

        // 수신자
        UserEntity receiver = em.getReference(UserEntity.class, data.receiverId());

        // 알림 생성
        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .targetType(data.targetType())
                .targetId(data.targetId())
                .payload(data.payload())
                .build();
        notificationRepository.save(notification);

        // 알림 outbox 생성
        NotificationOutboxEntity outbox = NotificationOutboxEntity.builder()
                .receiverId(data.receiverId())
                .payload(data.payload())
                .status(SendingStatus.READY)
                .build();
        NotificationOutboxEntity savedOutbox = outboxRepository.save(outbox);
        return new NotificationSendingInfo(savedOutbox.getId(), data.receiverId(),
                data.payload(), savedOutbox.getAttempt());
    }
}
