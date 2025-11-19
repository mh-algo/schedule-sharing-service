package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.common.exception.ServerErrorCode;
import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.domain.NotificationSendingInfo;
import com.minhyung.schedule.notification.domain.SendingStatus;
import com.minhyung.schedule.notification.domain.entity.NotificationEntity;
import com.minhyung.schedule.notification.domain.entity.NotificationMessageEntity;
import com.minhyung.schedule.notification.domain.entity.NotificationSendingEntity;
import com.minhyung.schedule.notification.repository.NotificationMessageRepository;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.repository.NotificationSendingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationNotificationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSendingRepository sendingRepository;
    private final NotificationMessageRepository messageRepository;
    private final UserService userService;

    @Transactional
    @Override
    public NotificationSendingInfo createNotification(NotificationData data) {
        // 메시지 조회
        NotificationMessageEntity message = messageRepository.findByType(data.messageType())
                .orElseThrow(() -> {
                    log.error("Message Not Found: {}", data.messageType());
                    return new ApiException(ServerErrorCode.SERVER_ERROR);
                });

        // 수신자 조회
        UserEntity receiver;
        try {
            receiver = userService.getUserEntity(data.receiverId());
        } catch (UserNotFoundException e) {
            log.error("Receiver Not Found: {}", data.receiverId());
            throw new ApiException(ServerErrorCode.SERVER_ERROR);
        }

        // 알림 생성
        NotificationEntity notification = NotificationEntity.builder()
                .message(message)
                .receiver(receiver)
                .targetType(data.targetType())
                .targetId(data.targetId())
                .payload(data.payload())
                .build();
        NotificationEntity savedNotification = notificationRepository.save(notification);

        // 알림 전송 생성
        NotificationSendingEntity sending = NotificationSendingEntity.builder()
                .notification(savedNotification)
                .status(SendingStatus.READY)
                .build();
        NotificationSendingEntity savedSending = sendingRepository.save(sending);
        return new NotificationSendingInfo(savedSending.getId(), savedNotification.getReceiver().getId(),
                savedNotification.getPayload(), savedSending.getAttempt());
    }
}
