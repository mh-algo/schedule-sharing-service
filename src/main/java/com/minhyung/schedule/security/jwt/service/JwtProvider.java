package com.minhyung.schedule.security.jwt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.security.jwt.JwtUtils;
import com.minhyung.schedule.security.jwt.dto.IssuedToken;
import com.minhyung.schedule.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class JwtProvider {
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-ttl-ms}")
    private long accessTokenTTLMs;

    @Value("${jwt.refresh-token-ttl-ms}")
    private long refreshTokenTTLMs;

    public JwtProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public IssuedToken issueAccess(UserPrincipal principal, Instant now) {
        String sub = String.valueOf(principal.id());
        Map<String, Object> claims = objectMapper.convertValue(principal, new TypeReference<>() {});
        Instant expiresAt = now.plusMillis(accessTokenTTLMs);
        String access = JwtUtils.encode(sub, claims, secretKey, now, expiresAt);
        return new IssuedToken(sub, access, expiresAt);
    }

    public IssuedToken issueRefresh(UserPrincipal principal, Instant now) {
        String sub = String.valueOf(principal.id());
        Instant expiresAt = now.plusMillis(refreshTokenTTLMs);
        String refresh = JwtUtils.encode(sub, secretKey, now, expiresAt);
        return new IssuedToken(sub, refresh, expiresAt);
    }

    public Claims parseClaims(String token) throws JwtException {
        return JwtUtils.decode(token, secretKey);
    }
}
