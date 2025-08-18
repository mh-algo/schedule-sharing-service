package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.auth.dto.UserInfoDto;

public final class TestUserInfoDtoBuilder {
    private Long id = 1L;
    private String username = "username";
    private String password = "{bcrypt}$2a$10$9tJM5zUrYimpTepZ5WraUuCvVxZZXMc2M4J92fTqMaECAnZRNkRGa";   // password123!
    private UserStatus status = UserStatus.ACTIVE;

    private TestUserInfoDtoBuilder() {}

    public static TestUserInfoDtoBuilder user() {
        return new TestUserInfoDtoBuilder();
    }

    public TestUserInfoDtoBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public TestUserInfoDtoBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public TestUserInfoDtoBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public TestUserInfoDtoBuilder withStatus(UserStatus status) {
        this.status = status;
        return this;
    }

    public UserInfoDto build() {
        return new UserInfoDto(id, username, password, status);
    }
}
