package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.NotificationData;
import com.minhyung.schedule.notification.domain.NotificationSendingInfo;

public interface NotificationService {
    NotificationSendingInfo createNotification(NotificationData data);
}
