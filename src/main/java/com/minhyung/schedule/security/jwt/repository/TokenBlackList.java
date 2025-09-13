package com.minhyung.schedule.security.jwt.repository;

import java.time.Instant;

public interface TokenBlackList {
    void save(String token, String reason, Instant expiresAt);
    boolean isBlackListed(String token);
}
