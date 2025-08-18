package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.security.jwt.JwtUtils;
import com.minhyung.schedule.testsupport.TestClock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class TestToken {
    private static final Clock DEFAULT_CLOCK = TestClock.fixedAt("2025-08-01T00:00:00Z");
    private static final String DEFAULT_SUB = "1";
    public static final String SECRET_KEY = "xkBS7dHQasgwoDllsgSMbIjfVSoY2f3IGyXvtmUor5k=";

    public static String access() {
        return access(DEFAULT_SUB, DEFAULT_CLOCK);
    }

    public static String refresh() {
        return refresh(DEFAULT_SUB, DEFAULT_CLOCK);
    }

    public static String access(String sub) {
        return access(sub, DEFAULT_CLOCK);
    }

    public static String refresh(String sub) {
        return refresh(sub, DEFAULT_CLOCK);
    }

    public static String access(Clock clock) {
        return access(DEFAULT_SUB, DEFAULT_CLOCK);
    }

    public static String refresh(Clock clock) {
        return refresh(DEFAULT_SUB, DEFAULT_CLOCK);
    }

    public static String access(String sub, Clock clock) {
        Instant now = Instant.now(clock);
        return JwtUtils.encode(sub, Map.of("verified", true), SECRET_KEY, now, now.plus(Duration.ofHours(1)));
    }

    public static String refresh(String sub, Clock clock) {
        Instant now = Instant.now(clock);
        return JwtUtils.encode(sub, SECRET_KEY, now, now.plus(Duration.ofDays(7)));
    }
}
