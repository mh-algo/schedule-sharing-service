package com.minhyung.schedule.auth.repository.converter;

import com.minhyung.schedule.auth.domain.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserStatusConverter implements AttributeConverter<UserStatus, Byte> {

    @Override
    public Byte convertToDatabaseColumn(UserStatus status) {
        return UserStatus.getCode(status);
    }

    @Override
    public UserStatus convertToEntityAttribute(Byte code) {
        return UserStatus.getType(code);
    }
}
