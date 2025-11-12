package com.minhyung.schedule.group.repository.converter;

import com.minhyung.schedule.common.AbstractCodeEnumConverter;
import com.minhyung.schedule.group.domain.GroupInviteStatus;
import jakarta.persistence.Converter;

@Converter
public class GroupInviteStatusConverter extends AbstractCodeEnumConverter<GroupInviteStatus, Byte> {
    public GroupInviteStatusConverter() {
        super(GroupInviteStatus.class);
    }
}
