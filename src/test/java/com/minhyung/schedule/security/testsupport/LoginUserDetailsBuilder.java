package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.auth.domain.UserStatus;
import com.minhyung.schedule.security.login.LoginUserDetails;
import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public final class LoginUserDetailsBuilder {
    private Long id = 1L;
    private String username = "username";
    private String password = "{bcrypt}$2a$10$9tJM5zUrYimpTepZ5WraUuCvVxZZXMc2M4J92fTqMaECAnZRNkRGa";   // password123!
    private UserStatus status = UserStatus.ACTIVE;
    private List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private LoginUserDetailsBuilder() {}

    public static LoginUserDetailsBuilder userDetails() {
        return new LoginUserDetailsBuilder();
    }

    public LoginUserDetailsBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public LoginUserDetailsBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public LoginUserDetailsBuilder withPassword(String password, PasswordEncoder encoder) {
        this.password = encoder.encode(password);
        return this;
    }

    public LoginUserDetailsBuilder withEncodedPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginUserDetailsBuilder withStatus(UserStatus status) {
        this.status = status;
        return this;
    }

    public LoginUserDetailsBuilder withAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
        return this;
    }

    public LoginUserDetails build() {
        return new LoginUserDetails(createUserInfo(), authorities);
    }

    private LoginUserInfo createUserInfo() {
        return LoginUserInfo.builder()
                .id(id)
                .username(username)
                .password(password)
                .status(status)
                .build();
    }
}
