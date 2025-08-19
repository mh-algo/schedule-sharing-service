package com.minhyung.schedule.security.jwt.scheduler;

import com.minhyung.schedule.security.jwt.repository.InMemoryTokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnBean(InMemoryTokenStore.class) // InMemory일 때만 구동
@ConditionalOnProperty(value="jwt.cleanup.enabled", havingValue="true", matchIfMissing=true)
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final InMemoryTokenStore inMemoryTokenStore;

    @Scheduled(
            fixedDelayString = "${jwt.cleanup.delay-ms}",
            initialDelayString = "${jwt.cleanup.initial-ms}"
    )
    public void run() {
        long t0 = System.nanoTime();
        int removed = inMemoryTokenStore.removeExpired();    // 만료된 토큰 제거
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);

        if (removed > 0) {
            log.info("token-cleanup: removed={}, took={}ms", removed, tookMs);
        } else {
            log.warn("token-cleanup: removed=0, took={}ms", tookMs);
        }
    }
}
