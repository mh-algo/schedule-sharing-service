package com.minhyung.schedule.notification.domain.entity;

import com.minhyung.schedule.notification.domain.MessageType;
import com.minhyung.schedule.notification.repository.converter.NotificationMessageTypeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter     // 알림 메시지 조회 목적 엔티티
public class NotificationMessageEntity {
    @Id
    private Long id;

    @Convert(converter = NotificationMessageTypeConverter.class)
    private MessageType type;

    @Column(insertable = false, updatable = false, nullable = false)
    private String title;

    @Column(insertable = false, updatable = false, nullable = false)
    private String message;
}
