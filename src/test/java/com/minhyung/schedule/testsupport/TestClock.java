package com.minhyung.schedule.testsupport;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class TestClock {
    private TestClock() {}

    public static Clock fixedAt(String iso) {
        return Clock.fixed(Instant.parse(iso), ZoneOffset.UTC);
    }
}
