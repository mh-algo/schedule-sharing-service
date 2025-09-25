package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.common.exception.ServerErrorCode;
import com.minhyung.schedule.notification.domain.SendingStatus;
import com.minhyung.schedule.notification.domain.entity.NotificationSendingEntity;
import com.minhyung.schedule.notification.repository.NotificationSendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationStatusService {
    private final NotificationSendingRepository sendingRepository;

    public NotificationStatusService(NotificationSendingRepository sendingRepository) {
        this.sendingRepository = sendingRepository;
    }

    public void changeToReady(Long id) {
        NotificationSendingEntity sending = sendingRepository.findById(id).orElseThrow(() -> {
            log.warn("조회되는 NotificationSendingEntity가 없습니다.: {}", id);
            return new ApiException(ServerErrorCode.SERVER_ERROR);
        });

        // sending 상태를 READY로 변경
        sending.updateStatus(SendingStatus.READY);
    }
}
