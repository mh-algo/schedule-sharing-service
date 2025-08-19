package com.minhyung.schedule.security.jwt;

public final class JwtHeader {
    public static final String ACCESS_TOKEN = "Authorization";
    public static final String REFRESH_TOKEN = "Refresh-Token";

    private JwtHeader() {
        throw new AssertionError("Cannot instantiate ApiPaths");    // 리플렉션 생성 방지
    }
}
