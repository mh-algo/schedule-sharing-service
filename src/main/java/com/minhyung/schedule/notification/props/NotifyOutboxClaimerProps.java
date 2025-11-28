package com.minhyung.schedule.notification.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notify.outbox.claimer")
public record NotifyOutboxClaimerProps(
        int baseIntervalMs,
        int maxIntervalMs,
        int batchSize
) {
}
