package com.minhyung.schedule.notification.repository.converter;

import com.minhyung.schedule.common.AbstractCodeEnumConverter;
import com.minhyung.schedule.notification.domain.MessageType;
import jakarta.persistence.Converter;

@Converter
public class NotificationMessageTypeConverter extends AbstractCodeEnumConverter<MessageType, Byte> {
    public NotificationMessageTypeConverter() {
        super(MessageType.class);
    }
}
