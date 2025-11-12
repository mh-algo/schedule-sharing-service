package com.minhyung.schedule.group.domain.entity;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.common.entity.TimeStamp;
import com.minhyung.schedule.group.domain.GroupMemberRoleType;
import com.minhyung.schedule.group.repository.converter.GroupMemberRoleTypeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GroupMemberEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Convert(converter = GroupMemberRoleTypeConverter.class)
    @Column(name = "role_type")
    private GroupMemberRoleType role;

    @Builder
    private GroupMemberEntity(GroupEntity group, UserEntity user, GroupMemberRoleType role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }
}
