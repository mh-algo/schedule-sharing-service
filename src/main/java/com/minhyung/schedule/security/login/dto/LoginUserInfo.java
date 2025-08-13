package com.minhyung.schedule.security.login.dto;

import com.minhyung.schedule.auth.domain.UserStatus;
import lombok.Builder;

@Builder
public record LoginUserInfo(
        Long id,
        String username,
        String password,
        UserStatus status
) {
}
