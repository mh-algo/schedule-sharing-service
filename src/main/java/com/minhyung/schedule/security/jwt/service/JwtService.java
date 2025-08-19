package com.minhyung.schedule.security.jwt.service;

import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.repository.TokenStore;
import com.minhyung.schedule.security.principal.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class JwtService {
    private final JwtProvider jwtProvider;
    private final TokenStore tokenStore;
    private final Clock clock;

    @Value("${jwt.refresh-token-ttl-ms}")
    private long refreshTokenTTLMs;

    public JwtService(JwtProvider jwtProvider, TokenStore tokenStore, Clock clock) {
        this.jwtProvider = jwtProvider;
        this.tokenStore = tokenStore;
        this.clock = clock;
    }

    public JwtToken createJwtToken(UserPrincipal principal) {
        String accessToken = jwtProvider.generateAccessToken(principal);
        String refreshToken = jwtProvider.generateRefreshToken(principal);
        String sub = principal.id().toString();
        Instant expiresAt = Instant.now(clock).plusMillis(refreshTokenTTLMs);
        tokenStore.save(sub, refreshToken, expiresAt);      // 생성된 refreshToken 저장
        return JwtToken.ofRaw(accessToken, refreshToken);
    }
}
