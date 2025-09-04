package com.minhyung.schedule.security.jwt.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnBean(TokenExpiryCleaner.class)
@ConditionalOnProperty(value="jwt.cleanup.enabled", havingValue="true", matchIfMissing=true)
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final Map<String, TokenExpiryCleaner> cleaners;

    @Scheduled(
            fixedDelayString = "10000", // "${jwt.cleanup.delay-ms}",
            initialDelayString = "0" // "${jwt.cleanup.initial-ms}"
    )
    public void run() {
        for (Map.Entry<String, TokenExpiryCleaner> entry : cleaners.entrySet()) {
            long t0 = System.nanoTime();

            String name = entry.getKey();
            TokenExpiryCleaner cleaner = entry.getValue();

            int removed = cleaner.removeExpired();    // 만료된 토큰 제거
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);

            if (removed > 0) {
                log.info("token-cleanup: cleaner={}, removed={}, took={}ms", name, removed, tookMs);
            } else {
                log.warn("token-cleanup: cleaner={}, removed=0, took={}ms", name, tookMs);
            }
        }
    }
}
