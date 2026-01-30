package com.minhyung.schedule.log;

import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HikariPoolLogger {
    private final HikariPoolMXBean hikariPoolMXBean;

    @Scheduled(fixedRate = 3000)
    public void logPoolStatus() {
        log.debug("Hikari pool - active: {}, idle: {}, total: {}, pending: {}",
                hikariPoolMXBean.getActiveConnections(),
                hikariPoolMXBean.getIdleConnections(),
                hikariPoolMXBean.getTotalConnections(),
                hikariPoolMXBean.getThreadsAwaitingConnection());
    }
}
