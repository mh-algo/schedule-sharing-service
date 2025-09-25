package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.domain.NotificationData;

public interface NotificationService {
    Long createNotification(NotificationData data);
}
