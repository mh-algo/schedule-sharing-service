package com.minhyung.schedule.security.jwt.dto;

import java.time.Instant;

public record TokenData(
        String token,
        Instant expiresAt
) {
    public boolean isSameToken(String token) {
        return this.token.equals(token);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);    // expiresAt 이상일 경우 만료
    }
}
