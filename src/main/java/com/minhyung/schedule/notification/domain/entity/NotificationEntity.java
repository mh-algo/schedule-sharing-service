package com.minhyung.schedule.notification.domain.entity;

import com.minhyung.schedule.auth.domain.entity.UserEntity;
import com.minhyung.schedule.common.entity.CreatedAndDeleted;
import com.minhyung.schedule.notification.domain.NotificationType;
import com.minhyung.schedule.notification.repository.converter.NotificationTypeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NotificationEntity extends CreatedAndDeleted {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private NotificationMessageEntity message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(name = "target_type")
    private NotificationType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Lob
    private String payload;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Builder
    public NotificationEntity(NotificationMessageEntity message, UserEntity receiver, NotificationType targetType, Long targetId, String payload) {
        this.message = message;
        this.receiver = receiver;
        this.targetType = targetType;
        this.targetId = targetId;
        this.payload = payload;
        this.isRead = false;
    }
}
