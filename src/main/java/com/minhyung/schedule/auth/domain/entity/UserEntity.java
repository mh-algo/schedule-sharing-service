package com.minhyung.schedule.auth.domain.entity;

import com.minhyung.schedule.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private String username;

    private String password;

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static UserEntity of(String username, String password) {
        return new UserEntity(username, password);
    }
}
