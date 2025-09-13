package com.minhyung.schedule.security.jwt.dto;

import java.time.Instant;

public record IssuedToken(
        String sub,
        String token,
        Instant expiresAt
) {
}
