package com.minhyung.schedule.notification.handler;

import com.minhyung.schedule.notification.event.NotificationEvent;

public interface NotificationCreateEventHandler {
    void onEvent(NotificationEvent event);
}
