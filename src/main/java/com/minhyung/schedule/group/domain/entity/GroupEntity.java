package com.minhyung.schedule.group.domain.entity;

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
    private Long ownerId;
    private String name;

    private GroupEntity(Long ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
    }

    public static GroupEntity createNew(Long ownerId, String name) {
        return new GroupEntity(ownerId, name);
    }
}
