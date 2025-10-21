package com.minhyung.schedule.notification.service;

import com.minhyung.schedule.notification.props.NotifyRetryProps;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class BackoffCalculator {
    private final NotifyRetryProps props;

    public BackoffCalculator(NotifyRetryProps props) {
        this.props = props;
    }

    public long calculate(int attempt) {
        double base = props.retryDelaySec() * Math.pow(2, attempt);
        double jitter = 1 + ThreadLocalRandom.current().nextDouble(-0.3, 0.3);    // +-30% (0.7 ~ 1.3)
        return Math.round(Math.min(base * jitter, props.maxRetryDelaySec()));
    }
}
