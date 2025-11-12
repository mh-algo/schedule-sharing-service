package com.minhyung.schedule.notification.event;

import com.minhyung.schedule.notification.domain.NotificationType;

public interface NotificationEvent {
    NotificationType type();
    Long id();
}
