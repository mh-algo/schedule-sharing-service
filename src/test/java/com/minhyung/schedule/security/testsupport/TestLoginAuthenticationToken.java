package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.security.login.LoginUserDetails;
import com.minhyung.schedule.security.principal.UserPrincipalMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public final class TestLoginAuthenticationToken {
    private static final String DEFAULT_USERNAME = "username";
    private static final String DEFAULT_PASSWORD = "password123!";
    private static final LoginUserDetails DEFAULT_USER_DETAILS = TestLoginUserDetailsBuilder.userDetails().build();

    private TestLoginAuthenticationToken() {}

    public static UsernamePasswordAuthenticationToken unauthenticated() {
        return unauthenticated(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public static UsernamePasswordAuthenticationToken unauthenticated(String username, String password) {
        return UsernamePasswordAuthenticationToken.unauthenticated(username, password);
    }

    public static UsernamePasswordAuthenticationToken authenticated() {
        return authenticated(DEFAULT_USER_DETAILS);
    }

    public static UsernamePasswordAuthenticationToken authenticated(LoginUserDetails userDetails) {
        return UsernamePasswordAuthenticationToken.authenticated(
                UserPrincipalMapper.from(userDetails), null, userDetails.getAuthorities());
    }
}
