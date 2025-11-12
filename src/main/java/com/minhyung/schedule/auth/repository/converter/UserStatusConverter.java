package com.minhyung.schedule.auth.repository.converter;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.common.AbstractCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class UserStatusConverter extends AbstractCodeEnumConverter<UserStatus, Byte> {
    public UserStatusConverter() {
        super(UserStatus.class);
    }
}
