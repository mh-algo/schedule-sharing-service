package com.minhyung.schedule.security.testsupport;

import com.minhyung.schedule.security.jwt.JwtToken;
import com.minhyung.schedule.security.jwt.JwtUtils;
import com.minhyung.schedule.testsupport.TestClock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class TestToken {
    private static final Clock DEFAULT_CLOCK = TestClock.now();
    private static final String DEFAULT_SUB = "1";
    public static final String SECRET_KEY = "xkBS7dHQasgwoDllsgSMbIjfVSoY2f3IGyXvtmUor5k=";
    public static final String HEADER_PREFIX = "Bearer ";

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

    public static JwtToken headers() {
        return headers(HEADER_PREFIX + access(), HEADER_PREFIX + refresh());
    }

    public static JwtToken headers(String accessHeader, String refreshHeader) {
        return JwtToken.ofBearer(accessHeader, refreshHeader);
    }

    public static String accessHeader() {
        return HEADER_PREFIX + access();
    }

    public static String refreshHeader() {
        return HEADER_PREFIX + refresh();
    }

    public static JwtToken tokens() {
        return tokens(access(), refresh());
    }

    public static JwtToken tokens(String sub) {
        return tokens(access(sub), refresh(sub));
    }

    public static JwtToken tokens(Clock clock) {
        return tokens(access(clock), refresh(clock));
    }

    public static JwtToken tokens(String sub, Clock clock) {
        return tokens(access(sub, clock), refresh(sub, clock));
    }

    public static JwtToken tokens(String access, String refresh) {
        return JwtToken.ofRaw(access, refresh);
    }
}
