package com.minhyung.schedule;

import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.service.JwtService;
import com.minhyung.schedule.security.principal.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@SpringBootTest
public abstract class AuthenticatedTest {
    private static final UserPrincipal PRINCIPAL = new UserPrincipal(1L, true);
    private JwtToken token;

    @Autowired
    void initToken(JwtService jwtService) {
        this.token = jwtService.issueJwtToken(PRINCIPAL);
    }

    protected final HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtHeader.ACCESS_TOKEN, token.getAuthHeader());
        headers.set(JwtHeader.REFRESH_TOKEN, token.getRefreshHeader());
        return headers;
    }

    protected final Authentication getAuthentication() {
        return UsernamePasswordAuthenticationToken.authenticated(
                PRINCIPAL, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
