package com.minhyung.schedule.notification.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notify.retry")
public record NotifyRetryProps(
        int delayMs,
        int retryDelaySec,
        int maxRetryDelaySec,
        int maxAttempts,
        int batchSize
) {
}
