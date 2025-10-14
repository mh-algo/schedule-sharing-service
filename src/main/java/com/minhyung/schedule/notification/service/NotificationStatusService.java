package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.repository.NotificationSendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NotificationStatusService {
    private final NotificationSendingRepository sendingRepository;

    @Value("${notify.lease.seconds}")
    private int leaseSec;

    public NotificationStatusService(NotificationSendingRepository sendingRepository) {
        this.sendingRepository = sendingRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean changeReady(Long id) {
        int rows = sendingRepository.updateReadyById(id);
        return rows == 1;
    }

    @Transactional
    public boolean changeProgressing(Long id) {
        int rows = sendingRepository.updateProgressing(id, leaseSec);
        return rows == 1;
    }

    @Transactional
    public boolean changeSent(Long id) {
        int rows = sendingRepository.updateSent(id);
        return rows == 1;
    }
}
