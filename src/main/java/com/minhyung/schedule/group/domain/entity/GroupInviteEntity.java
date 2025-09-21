package com.minhyung.schedule.group.domain.entity;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.common.entity.CreatedOnly;
import com.minhyung.schedule.group.domain.GroupInviteStatus;
import com.minhyung.schedule.group.repository.converter.GroupInviteStatusConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_invites")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GroupInviteEntity extends CreatedOnly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id")
    private UserEntity inviter;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id")
    private UserEntity invitee;

    @Convert(converter = GroupInviteStatusConverter.class)
    private GroupInviteStatus status;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "expires_at", updatable = false, nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    private void onCreate() {
        this.expiresAt = getCreatedAt().plusDays(7);
    }

    @Builder
    private GroupInviteEntity(GroupEntity group, UserEntity inviter, UserEntity invitee, GroupInviteStatus status) {
        this.group = group;
        this.inviter = inviter;
        this.invitee = invitee;
        this.status = status;
    }
}
