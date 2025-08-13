package com.minhyung.schedule.security.login.dto;

public record LoginRequest(
        String username,
        String password
) {
}
