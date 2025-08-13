package com.minhyung.schedule.auth.dto;

import com.minhyung.schedule.auth.domain.UserStatus;

public record UserInfoDto(
        Long id,
        String username,
        String password,
        UserStatus status
) {
}
