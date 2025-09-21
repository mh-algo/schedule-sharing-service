package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
}
