package com.minhyung.schedule.notification.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notify.outbox.create")
public record NotifyOutboxCreateProps(
        int baseIntervalMs,
        int maxIntervalMs,
        int batchSize
) {
}
