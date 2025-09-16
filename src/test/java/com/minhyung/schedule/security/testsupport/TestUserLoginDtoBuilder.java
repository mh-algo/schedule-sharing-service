package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.auth.dto.UserLoginDto;

public final class TestUserLoginDtoBuilder {
    private Long id = 1L;
    private String username = "username";
    private String password = "{bcrypt}$2a$10$9tJM5zUrYimpTepZ5WraUuCvVxZZXMc2M4J92fTqMaECAnZRNkRGa";   // password123!
    private UserStatus status = UserStatus.ACTIVE;

    private TestUserLoginDtoBuilder() {}

    public static TestUserLoginDtoBuilder user() {
        return new TestUserLoginDtoBuilder();
    }

    public TestUserLoginDtoBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public TestUserLoginDtoBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public TestUserLoginDtoBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public TestUserLoginDtoBuilder withStatus(UserStatus status) {
        this.status = status;
        return this;
    }

    public UserLoginDto build() {
        return new UserLoginDto(id, username, password, status);
    }
}
