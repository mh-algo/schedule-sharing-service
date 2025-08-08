package com.minhyung.schedule.common;

public final class ApiPaths {
    public static final String BASE_API = "/api/v1";
    public static final String AUTH = BASE_API + "/auths";
    public static final String MEMBER = BASE_API + "/members";

    private ApiPaths() {
        throw new AssertionError("Cannot instantiate ApiPaths");    // 리플렉션 생성 방지
    }
}
