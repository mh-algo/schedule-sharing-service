package com.minhyung.schedule.security.jwt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.security.jwt.JwtUtils;
import com.minhyung.schedule.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Component
public class JwtProvider {
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-ttl-ms}")
    private long accessTokenTTLMs;

    @Value("${jwt.refresh-token-ttl-ms}")
    private long refreshTokenTTLMs;

    public JwtProvider(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public String generateAccessToken(UserPrincipal principal) {
        String sub = String.valueOf(principal.id());
        Map<String, Object> claims = objectMapper.convertValue(principal, new TypeReference<>() {});
        Instant now = getNow();
        Instant expiresAt = now.plusMillis(accessTokenTTLMs);
        return JwtUtils.encode(sub, claims, secretKey, now, expiresAt);
    }

    public String generateRefreshToken(UserPrincipal principal) {
        String sub = String.valueOf(principal.id());
        Instant now = getNow();
        Instant expiresAt = now.plusMillis(refreshTokenTTLMs);
        return JwtUtils.encode(sub, secretKey, now, expiresAt);
    }

    public Claims parseClaims(String token) throws JwtException {
        return JwtUtils.decode(token, secretKey);
    }

    private Instant getNow() {
        return Instant.now(clock);
    }
}
