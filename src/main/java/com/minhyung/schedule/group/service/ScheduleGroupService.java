package com.minhyung.schedule.group.service;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.auth.exception.UserNotFoundException;
import com.minhyung.schedule.auth.exception.UserServiceErrorCode;
import com.minhyung.schedule.auth.repository.UserRepository;
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
import com.minhyung.schedule.notification.domain.Inviter;
import com.minhyung.schedule.notification.domain.NotificationType;
import com.minhyung.schedule.notification.event.InvitationCreatedEvent;
import com.minhyung.schedule.group.repository.GroupInviteRepository;
import com.minhyung.schedule.group.repository.GroupMemberRepository;
import com.minhyung.schedule.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleGroupService {
    private final GroupRepository groupRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

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
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #userId")
    public void invite(Long userId, Long groupId, InviteUserRequest request) {
        long start = System.currentTimeMillis();

        // 초대 그룹
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(GroupErrorCode.INVALID_ACCESS));

        // 초대한 사람 (초대한 사람이 그룹 소속인지 검증)
        UserEntity inviter = groupMemberRepository.findInviterCandidate(userId, groupId)
                .orElseThrow(() -> new ApiException(GroupErrorCode.INVALID_ACCESS));

        // 초대 받는 사람
        UserEntity invitee = userRepository.findInvitableUser(request.username(), groupId)
                .orElseThrow(() -> new ApiException(GroupErrorCode.USER_ALREADY_EXISTS_OR_INVITED));

        // 그룹 초대 생성
        GroupInviteEntity entity = GroupInviteEntity.builder()
                .group(group)
                .inviter(inviter)
                .invitee(invitee)
                .status(GroupInviteStatus.PENDING)
                .build();

        GroupInviteEntity invite = groupInviteRepository.save(entity);

        // 초대 알림 이벤트 발행
        InvitationCreatedEvent event = InvitationCreatedEvent.builder()
                .type(NotificationType.INVITE_CREATED)
                .id(invite.getId())                                                 // 생성된 group_invites id
                .groupId(group.getId())                                             // 초대된 group id
                .groupName(group.getName())                                         // 초대된 groupName
                .inviter(new Inviter(inviter.getId(), inviter.getUsername()))       // 초대한 사람
                .inviteeId(invitee.getId())                                         // 초대된 사람
                .createdAt(invite.getCreatedAt())
                .expiresAt(invite.getExpiresAt())
                .build();

        eventPublisher.publishEvent(event);
        log.debug("[INVITE] total service time = {} ms", (System.currentTimeMillis() - start));
    }
}
