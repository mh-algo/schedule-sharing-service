package com.minhyung.schedule.group.repository.converter;

import com.minhyung.schedule.group.domain.GroupInviteStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GroupInviteStatusConverter implements AttributeConverter<GroupInviteStatus, Byte> {
    @Override
    public Byte convertToDatabaseColumn(GroupInviteStatus status) {
        return GroupInviteStatus.getCode(status);
    }

    @Override
    public GroupInviteStatus convertToEntityAttribute(Byte code) {
        return GroupInviteStatus.getType(code);
    }
}
