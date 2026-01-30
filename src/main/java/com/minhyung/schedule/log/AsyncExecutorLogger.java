package com.minhyung.schedule.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class AsyncExecutorLogger {
    private final ThreadPoolTaskExecutor executor;
    private final AsyncExecStats stats;

    public AsyncExecutorLogger(@Qualifier("defaultAsyncExecutor") ThreadPoolTaskExecutor executor, AsyncExecStats stats) {
        this.executor = executor;
        this.stats = stats;
    }

    @Scheduled(fixedRate = 1000)
    public void logThreadPoolStatus() {
        ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
        int queue = threadPoolExecutor.getQueue().size();
        if (queue > 0) {
            log.info(
                    "[ASYNC] active={}, pool={}, core={}, max={}, queue={}, completed={}",
                    threadPoolExecutor.getActiveCount(),
                    threadPoolExecutor.getPoolSize(),
                    threadPoolExecutor.getCorePoolSize(),
                    threadPoolExecutor.getMaximumPoolSize(),
                    queue,
                    threadPoolExecutor.getCompletedTaskCount()
            );
        }
    }

    @Scheduled(fixedRate = 1000)
    public void logAsyncTaskDelayStats() {
        AsyncExecStats.Snapshot snapshot = stats.snapshotAndReset();
        if (snapshot.isEmpty()) return;

        long maxQueueMs = snapshot.maxQueueMs();
        long maxRunMs = snapshot.maxRunMs();
        if (maxQueueMs >= 200 || maxRunMs >= 500) {
            log.warn("[ASYNC_STAT][1s] count={} avgQueue={}ms maxQueue={}ms avgRun={}ms maxRun={}ms",
                    snapshot.count(), snapshot.avgQueueMs(), maxQueueMs, snapshot.avgRunMs(), maxRunMs);
        }
    }
}
