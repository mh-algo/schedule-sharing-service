package com.minhyung.schedule.security.jwt.dto;

import java.time.Instant;

public record BlackListedToken(
        String reason,
        Instant expiresAt
) {
    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);    // expiresAt 이상일 경우 만료
    }
}
