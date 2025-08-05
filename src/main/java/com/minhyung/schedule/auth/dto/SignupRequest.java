package com.minhyung.schedule.auth.dto;

public record SignupRequest(
        String username,
        String password,
        String passwordConfirm
) {
}
