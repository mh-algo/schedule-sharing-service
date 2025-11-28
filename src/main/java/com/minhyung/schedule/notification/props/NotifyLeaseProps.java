package com.minhyung.schedule.notification.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notify.lease")
public record NotifyLeaseProps(
        int seconds
) {
}
