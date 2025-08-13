package com.minhyung.schedule.security.principal;

public record UserPrincipal(
        Long id,
        boolean verified
) {
}
