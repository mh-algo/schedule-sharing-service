package com.minhyung.schedule.group.service;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.exception.UserServiceErrorCode;
import com.minhyung.schedule.auth.service.UserService;
import com.minhyung.schedule.common.exception.ApiException;
import com.minhyung.schedule.group.domain.GroupInviteStatus;
import com.minhyung.schedule.group.domain.GroupMemberRoleType;
import com.minhyung.schedule.group.domain.entity.GroupEntity;
import com.minhyung.schedule.group.domain.entity.GroupInviteEntity;
import com.minhyung.schedule.group.domain.entity.GroupMemberEntity;
import com.minhyung.schedule.group.dto.CreateGroupRequest;
import com.minhyung.schedule.group.dto.CreateGroupResponse;
import com.minhyung.schedule.group.dto.InviteUserRequest;
import com.minhyung.schedule.group.exception.GroupErrorCode;
import com.minhyung.schedule.group.repository.GroupInviteRepository;
import com.minhyung.schedule.group.repository.GroupMemberRepository;
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
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;

    @Transactional
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #id")
    public CreateGroupResponse create(Long id, CreateGroupRequest request) {
        try {
            // 그룹 생성자
            UserEntity owner = userService.getUserEntity(id);

            // 그룹 생성
            GroupEntity group = GroupEntity.createNew(owner, request.name());
            GroupEntity savedGroupEntity = groupRepository.save(group);

            // 그룹원 등록
            GroupMemberEntity member = GroupMemberEntity.builder()
                    .group(group)
                    .user(owner)
                    .role(GroupMemberRoleType.OWNER)
                    .build();
            groupMemberRepository.save(member);

            return new CreateGroupResponse(savedGroupEntity.getId(), savedGroupEntity.getOwner().getId(), savedGroupEntity.getName());
        } catch (UserNotFoundException e) {
            throw new ApiException(UserServiceErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #id")
    public void invite(Long id, Long groupId, InviteUserRequest request) {
        try {
            // 초대 그룹
            GroupEntity group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ApiException(GroupErrorCode.INVALID_ACCESS));

            // 초대한 사람
            UserEntity inviter = userService.getUserEntity(id);

            // 초대한 사람이 그룹 소속인지 검증
            groupMemberRepository.existsByUserId(inviter.getId())
                    .orElseThrow(() -> new ApiException(GroupErrorCode.INVALID_ACCESS));

            // 초대 받는 사람
            UserEntity invitee = userService.getUserEntity(request.username());

            // 초대 받는 사람이 그룹 소속이 아닌지 검증
            groupMemberRepository.existsByUserId(invitee.getId())
                    .ifPresent(inviteeExists -> {
                        throw new ApiException(GroupErrorCode.USER_ALREADY_EXISTS);
                    });

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
