package com.minhyung.schedule;

import com.minhyung.schedule.security.jwt.JwtHeader;
import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;

@SpringBootTest
public abstract class AuthenticatedIntegrationTest extends AuthenticatedTest {
    private JwtToken token;

    @Autowired
    void initToken(JwtService jwtService) {
        this.token = jwtService.issueJwtToken(getPrincipal());
    }

    protected final HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtHeader.ACCESS_TOKEN, token.getAuthHeader());
        headers.set(JwtHeader.REFRESH_TOKEN, token.getRefreshHeader());
        return headers;
    }
}
