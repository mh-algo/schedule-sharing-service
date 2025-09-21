package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.entity.NotificationSendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSendingRepository extends JpaRepository<NotificationSendingEntity, Long> {
}
