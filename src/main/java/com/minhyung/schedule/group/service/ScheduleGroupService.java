package com.minhyung.schedule.group.service;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.exception.UserServiceErrorCode;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.group.domain.GroupInviteStatus;
import com.minhyung.schedule.group.domain.entity.GroupEntity;
import com.minhyung.schedule.group.domain.entity.GroupInviteEntity;
import com.minhyung.schedule.group.dto.CreateGroupRequest;
import com.minhyung.schedule.group.dto.CreateGroupResponse;
import com.minhyung.schedule.group.dto.InviteUserRequest;
import com.minhyung.schedule.group.repository.GroupInviteRepository;
import com.minhyung.schedule.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleGroupService {
    private final GroupRepository groupRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final UserService userService;

    @Transactional
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #id")
    public CreateGroupResponse create(Long id, CreateGroupRequest request) {
        // 그룹 생성
        GroupEntity groupEntity = GroupEntity.createNew(id, request.name());
        GroupEntity savedGroupEntity = groupRepository.save(groupEntity);
        return new CreateGroupResponse(savedGroupEntity.getId(), savedGroupEntity.getOwnerId(), savedGroupEntity.getName());
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #id")
    public void invite(Long id, Long groupId, InviteUserRequest request) {
        try {
            // 초대 그룹
            GroupEntity group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(UserServiceErrorCode.USER_NOT_FOUND));

            // TODO: inviter가 그룹 소속인지 검증
            // TODO: invitee가 그룹 소속이 아닌지 검증

            // 초대한 사람
            UserEntity inviter = userService.getUserEntity(id);

            // 초대 받는 사람
            UserEntity invitee = userService.getUserEntity(request.username());

            // 그룹 초대 생성
            GroupInviteEntity entity = GroupInviteEntity.builder()
                    .groupId(group)
                    .inviterId(inviter)
                    .inviteeId(invitee)
                    .status(GroupInviteStatus.PENDING)
                    .build();

            groupInviteRepository.save(entity);
        } catch (UserNotFoundException e) {
            throw new ApiException(UserServiceErrorCode.USER_NOT_FOUND);
        }
    }
}
