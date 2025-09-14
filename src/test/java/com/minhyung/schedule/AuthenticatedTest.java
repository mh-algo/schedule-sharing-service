package com.minhyung.schedule;

import com.minhyung.schedule.security.principal.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public abstract class AuthenticatedTest {
    private static final UserPrincipal PRINCIPAL = new UserPrincipal(1L, true);

    protected final Authentication getAuthentication() {
        return UsernamePasswordAuthenticationToken.authenticated(
                PRINCIPAL, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    protected final UserPrincipal getPrincipal() {
        return PRINCIPAL;
    }
}
