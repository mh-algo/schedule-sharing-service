package com.minhyung.schedule.notification.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notify.outbox.committer")
public record NotifyOutboxCommitterProps(
        int baseIntervalMs,
        int maxIntervalMs,
        int batchSize
) {
}
