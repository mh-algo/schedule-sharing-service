package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.security.login.LoginUserDetails;
import com.minhyung.schedule.security.login.dto.LoginUserInfo;
import com.minhyung.schedule.security.principal.UserPrincipal;
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
                toUserPrincipal(userDetails), null, userDetails.getAuthorities());
    }

    private static UserPrincipal toUserPrincipal(LoginUserDetails userDetails) {
        LoginUserInfo account = userDetails.getUserInfo();
        boolean verified = !userDetails.isUnverified();
        return new UserPrincipal(account.id(), verified);
    }
}
