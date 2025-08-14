package com.minhyung.schedule.common;

import java.util.regex.Pattern;

public final class UsernameRules {
    public static final String REGEX = "^[a-zA-Z0-9]{4,12}$";
    public static final Pattern PATTERN = Pattern.compile(REGEX);

    private UsernameRules() {
        throw new AssertionError("Cannot instantiate ApiPaths");    // 리플렉션 생성 방지
    }
}
