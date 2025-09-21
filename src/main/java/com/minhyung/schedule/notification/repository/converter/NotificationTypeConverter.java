package com.minhyung.schedule.notification.repository.converter;

import com.minhyung.schedule.common.AbstractCodeEnumConverter;
import com.minhyung.schedule.notification.domain.NotificationType;
import jakarta.persistence.Converter;

@Converter
public class NotificationTypeConverter extends AbstractCodeEnumConverter<NotificationType, Byte> {
    public NotificationTypeConverter() {
        super(NotificationType.class);
    }
}
