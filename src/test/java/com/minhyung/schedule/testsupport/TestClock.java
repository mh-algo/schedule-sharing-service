package com.minhyung.schedule.testsupport;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class TestClock {
    private TestClock() {}

    private static final String NOW = "2025-08-01T00:00:00Z";

    public static Clock now() {
        return fixedAt(NOW);
    }

    public static Clock fixedAt(String iso) {
        return Clock.fixed(Instant.parse(iso), ZoneOffset.UTC);
    }
}
