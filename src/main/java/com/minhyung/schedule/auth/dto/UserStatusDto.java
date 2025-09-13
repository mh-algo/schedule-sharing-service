package com.minhyung.schedule.auth.dto;

import com.minhyung.schedule.auth.domain.UserStatus;

public record UserStatusDto(
        Long id,
        UserStatus status
) {
}
