package com.minhyung.schedule.group.service;

import com.minhyung.schedule.group.domain.entity.GroupEntity;
import com.minhyung.schedule.group.dto.CreateGroupRequest;
import com.minhyung.schedule.group.dto.CreateGroupResponse;
import com.minhyung.schedule.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleGroupService {
    private final GroupRepository groupRepository;

    @Transactional
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #id")
    public CreateGroupResponse create(Long id, CreateGroupRequest request) {
        GroupEntity groupEntity = GroupEntity.createNew(id, request.name());
        GroupEntity savedGroupEntity = groupRepository.save(groupEntity);
        return new CreateGroupResponse(savedGroupEntity.getId(), savedGroupEntity.getOwnerId(), savedGroupEntity.getName());
    }
}
