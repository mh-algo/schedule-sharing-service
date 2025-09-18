package com.minhyung.schedule.group.domain.entity;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`groups`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GroupEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    private String name;

    public GroupEntity(UserEntity owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public static GroupEntity createNew(UserEntity owner, String name) {
        return new GroupEntity(owner, name);
    }
}
