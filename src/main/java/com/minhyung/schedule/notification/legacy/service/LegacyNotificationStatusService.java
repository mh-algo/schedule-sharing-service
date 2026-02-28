package com.minhyung.schedule.notification.legacy.service;

import com.minhyung.schedule.notification.domain.NotificationRetryInfo;
import com.minhyung.schedule.notification.legacy.domain.RetryInfo;
import com.minhyung.schedule.notification.repository.NotificationRepository;
import com.minhyung.schedule.notification.legacy.repository.LegacyNotificationSendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Deprecated(forRemoval = true)
@Slf4j
public class LegacyNotificationStatusService {
    private final LegacyNotificationSendingRepository sendingRepository;
    private final NotificationRepository notificationRepository;

    @Value("${notify.lease.seconds}")
    private int leaseSec;

    public LegacyNotificationStatusService(LegacyNotificationSendingRepository sendingRepository, NotificationRepository notificationRepository) {
        this.sendingRepository = sendingRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean changeReady(Long id) {
        int rows = sendingRepository.updateReadyById(id);
        return rows == 1;
    }

    @Transactional
    public boolean changeProgressing(Long id, int attempt) {
        int rows = sendingRepository.updateProgressing(id, leaseSec, attempt);
        return rows == 1;
    }

    @Transactional
    public boolean changeSent(Long id) {
        int rows = sendingRepository.updateSent(id);
        return rows == 1;
    }

    @Transactional
    public boolean changeFailed(Long id) {
        int rows = sendingRepository.updateFailed(id);
        return rows == 1;
    }

    @Transactional
    public boolean changeRetryPending(Long id, String error, long backoffSec) {
        int rows = sendingRepository.updateRetryPending(id, error, backoffSec);
        return rows == 1;
    }

    @Transactional
    public boolean changeRetryPending(Long id, String error, long backoffSec, int attempt) {
        int rows = sendingRepository.updateRetryPending(id, error, backoffSec, attempt);
        return rows == 1;
    }

    @Transactional
    public void changeProgressingExpiredToRetryPending(int batchSize) {
        sendingRepository.updateProgressingExpiredToRetryPending(batchSize);
    }

    @Transactional
    public boolean changeRetryPendingWhenQueuePublishFailed(Long id, String error, long backoffSec, int attempt) {
        int rows = sendingRepository.updateRetryPending(id, error, backoffSec, attempt);
        return rows == 1;
    }

    @Transactional
    public List<RetryInfo> changeRetryPendingToReadyOrFailed(int batchSize, int maxAttempts) {
        // skip locked
        List<RetryInfo> retryInfoList = sendingRepository.findDueRetryIds(batchSize);
        List<RetryInfo> retryIds = new ArrayList<>();
        List<Long> failIds = new ArrayList<>();

        // attempt가 maxAttempts보다 작은 경우 retry, 크거나 같을 경우 fail
        retryInfoList.forEach(retryInfo -> {
            if (retryInfo.attempt() < maxAttempts) {
                retryIds.add(retryInfo);
            } else {
                failIds.add(retryInfo.sendingId());
            }
        });

        // READY
        if (!retryIds.isEmpty()) {
            List<Long> retryList = retryIds.stream().map(RetryInfo::sendingId).toList();
            sendingRepository.updateReadyByIds(retryList);
        }

        // FAILED
        if (!failIds.isEmpty()) {
            sendingRepository.updateFailedByIds(failIds);
        }

        return retryIds;
    }

    @Transactional(readOnly = true)
    public List<NotificationRetryInfo> findRetryInfoByNotificationIds(List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return List.of();
        }
        return notificationRepository.findRetryInfoByIds(notificationIds);
    }
}
