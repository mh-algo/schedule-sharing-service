package com.minhyung.schedule.notification.repository.converter;

import com.minhyung.schedule.common.AbstractCodeEnumConverter;
import com.minhyung.schedule.notification.domain.SendingStatus;
import jakarta.persistence.Converter;

@Converter
public class SendingStatusConverter extends AbstractCodeEnumConverter<SendingStatus, Byte> {
    protected SendingStatusConverter() {
        super(SendingStatus.class);
    }
}
