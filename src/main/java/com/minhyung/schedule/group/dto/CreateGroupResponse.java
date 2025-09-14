package com.minhyung.schedule.group.dto;

public record CreateGroupResponse(
        Long id,
        Long ownerId,
        String groupName
) {
}
