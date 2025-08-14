package com.minhyung.schedule.security.jwt.repository;

import com.minhyung.schedule.security.jwt.dto.TokenData;

import java.time.Instant;
import java.util.Optional;

public interface TokenStore {
    void save(String sub, String refreshToken, Instant expiresAt);
    void remove(String sub);
    boolean isInvalid(String sub, String refreshToken);
    Optional<TokenData> find(String sub);
}
