package com.minhyung.schedule.log;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Component
public class AsyncExecStats {
    private final LongAdder count = new LongAdder();
    private final LongAdder sumQueueMs = new LongAdder();
    private final LongAdder sumRunMs = new LongAdder();
    private final AtomicLong maxQueueMs = new AtomicLong();
    private final AtomicLong maxRunMs = new AtomicLong();

    public void record(long queueDelayMs, long runMs) {
        count.increment();
        sumQueueMs.add(queueDelayMs);
        sumRunMs.add(runMs);
        maxQueueMs.accumulateAndGet(queueDelayMs, Math::max);
        maxRunMs.accumulateAndGet(runMs, Math::max);
    }

    public Snapshot snapshotAndReset() {
        long totalQueue = sumQueueMs.sumThenReset();
        long totalRun = sumRunMs.sumThenReset();
        long maxQueue = maxQueueMs.getAndSet(0);
        long maxRun = maxRunMs.getAndSet(0);
        long c = count.sumThenReset();

        if (c == 0) return Snapshot.empty();

        long avgQueue = totalQueue / c;
        long avgRun = totalRun / c;
        return new Snapshot(c, avgQueue, maxQueue, avgRun, maxRun);
    }

    public record Snapshot(long count, long avgQueueMs, long maxQueueMs, long avgRunMs, long maxRunMs) {
        static Snapshot empty() {
            return new Snapshot(0,0,0,0,0);
        }

        public boolean isEmpty() {
            return count == 0;
        }
    }
}
