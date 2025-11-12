package com.minhyung.schedule.notification.encoder;

import com.minhyung.schedule.notification.domain.MessageEnvelope;
import com.minhyung.schedule.notification.event.NotificationEvent;

public interface PayloadEncoder<T extends NotificationEvent> {
    MessageEnvelope toEnvelope(T event);
    Class<T> supports();
}
