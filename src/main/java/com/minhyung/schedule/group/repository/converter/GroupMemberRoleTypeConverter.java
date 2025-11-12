package com.minhyung.schedule.group.repository.converter;

import com.minhyung.schedule.common.AbstractCodeEnumConverter;
import com.minhyung.schedule.group.domain.GroupMemberRoleType;
import jakarta.persistence.Converter;

@Converter
public class GroupMemberRoleTypeConverter extends AbstractCodeEnumConverter<GroupMemberRoleType, Byte> {
    public GroupMemberRoleTypeConverter() {
        super(GroupMemberRoleType.class);
    }
}
