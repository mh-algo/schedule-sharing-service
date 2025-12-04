package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.OutboxInfo;
import com.minhyung.schedule.notification.domain.entity.NotificationOutboxEntity;
import com.minhyung.schedule.notification.props.NotifyLeaseProps;
import com.minhyung.schedule.notification.repository.NotificationOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationOutboxService {
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotifyLeaseProps notifyLeaseProps;

    public NotificationOutboxService(NotificationOutboxRepository notificationOutboxRepository, NotifyLeaseProps notifyLeaseProps) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.notifyLeaseProps = notifyLeaseProps;
    }

    @Transactional
    public List<OutboxInfo> claimBatch(int batchSize) {
        // 전송할 알림 outbox 조회
        List<NotificationOutboxEntity> outboxList =
                notificationOutboxRepository.findReadyForClaim(PageRequest.of(0, batchSize));

        if (outboxList.isEmpty()) {
            return List.of();
        }

        // id 리스트 추출
        List<Long> ids = outboxList.stream()
                .map(NotificationOutboxEntity::getId)
                .toList();

        // 메시지를 점유할 수 있는 상한 시간
        LocalDateTime t = LocalDateTime.now().plusSeconds(notifyLeaseProps.seconds());

        // PROGRESSING 설정
        notificationOutboxRepository.updateProgressing(ids, t);

        return outboxList.stream()
                .map(entity -> OutboxInfo.of(
                        entity.getId(),
                        entity.getReceiverId(),
                        entity.getPayload(),
                        entity.getAttempt()
                )).toList();
    }

    @Transactional
    public int deleteAll(List<Long> ids) {
        if (ids.isEmpty()) return 0;
        return notificationOutboxRepository.deleteAllByIds(ids);
    }

    @Transactional
    public int updateRetryPending(Long id, LocalDateTime nextPushAt, String error) {
        return notificationOutboxRepository.updateRetryPending(id, nextPushAt, error);
    }
}
