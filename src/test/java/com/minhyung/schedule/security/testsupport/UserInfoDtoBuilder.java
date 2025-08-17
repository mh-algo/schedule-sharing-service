package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.auth.dto.UserInfoDto;

public final class UserInfoDtoBuilder {
    private Long id = 1L;
    private String username = "username";
    private String password = "{bcrypt}$2a$10$9tJM5zUrYimpTepZ5WraUuCvVxZZXMc2M4J92fTqMaECAnZRNkRGa";   // password123!
    private UserStatus status = UserStatus.ACTIVE;

    private UserInfoDtoBuilder() {}

    public static UserInfoDtoBuilder user() {
        return new UserInfoDtoBuilder();
    }

    public UserInfoDtoBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public UserInfoDtoBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserInfoDtoBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserInfoDtoBuilder withStatus(UserStatus status) {
        this.status = status;
        return this;
    }

    public UserInfoDto build() {
        return new UserInfoDto(id, username, password, status);
    }
}
