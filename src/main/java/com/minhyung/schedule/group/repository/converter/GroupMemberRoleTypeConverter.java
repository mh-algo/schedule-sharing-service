package com.minhyung.schedule.group.repository.converter;

import com.minhyung.schedule.group.domain.GroupMemberRoleType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GroupMemberRoleTypeConverter implements AttributeConverter<GroupMemberRoleType, Byte> {
    @Override
    public Byte convertToDatabaseColumn(GroupMemberRoleType type) {
        return GroupMemberRoleType.getCode(type);
    }

    @Override
    public GroupMemberRoleType convertToEntityAttribute(Byte code) {
        return GroupMemberRoleType.getType(code);
    }
}
