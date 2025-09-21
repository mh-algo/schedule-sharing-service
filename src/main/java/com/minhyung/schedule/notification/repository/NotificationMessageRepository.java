package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.MessageType;
import com.minhyung.schedule.notification.domain.entity.NotificationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationMessageRepository extends JpaRepository<NotificationMessageEntity, Long> {
    Optional<NotificationMessageEntity> findByType(MessageType type);
}
