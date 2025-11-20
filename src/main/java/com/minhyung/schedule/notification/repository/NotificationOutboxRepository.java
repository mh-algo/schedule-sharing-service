package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.entity.NotificationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxEntity, Long> {
}
