package com.minhyung.schedule.auth.domain.entity;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.auth.repository.converter.UserStatusConverter;
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

    @Convert(converter = UserStatusConverter.class)
    private UserStatus status;

    public UserEntity(String username, String password, UserStatus status) {
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public static UserEntity of(String username, String password, UserStatus status) {
        return new UserEntity(username, password, status);
    }
}
