package com.minhyung.schedule.security.jwt.service;

import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.dto.IssuedToken;
import com.minhyung.schedule.security.jwt.repository.TokenStore;
import com.minhyung.schedule.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class JwtService {
    private final JwtProvider jwtProvider;
    private final TokenStore tokenStore;
    private final Clock clock;

    public JwtService(JwtProvider jwtProvider, TokenStore tokenStore, Clock clock) {
        this.jwtProvider = jwtProvider;
        this.tokenStore = tokenStore;
        this.clock = clock;
    }

    public JwtToken createJwtToken(UserPrincipal principal) {
        Instant now = Instant.now(clock);
        IssuedToken access = jwtProvider.issueAccess(principal, now);
        IssuedToken refresh = jwtProvider.issueRefresh(principal, now);
        tokenStore.save(refresh.sub(), refresh.token(), refresh.expiresAt());      // 생성된 refreshToken 저장
        return JwtToken.ofRaw(access.token(), refresh.token());
    }

    public Claims parseClaims(String token) throws IllegalArgumentException, JwtException {
        return jwtProvider.parseClaims(token);
    }
}
