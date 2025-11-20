package com.minhyung.schedule.notification.domain.entity;

import com.minhyung.schedule.common.entity.CreatedAndUpdated;
import com.minhyung.schedule.notification.domain.SendingStatus;
import com.minhyung.schedule.notification.repository.converter.SendingStatusConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NotificationOutboxEntity extends CreatedAndUpdated {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String payload;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Convert(converter = SendingStatusConverter.class)
    private SendingStatus status;

    private Integer attempt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "lease_until")
    private LocalDateTime leaseUntil;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_push_at")
    private LocalDateTime nextPushAt;

    @Builder
    public NotificationOutboxEntity(Long receiverId, String payload, SendingStatus status) {
        this.receiverId = receiverId;
        this.payload = payload;
        this.status = status;
        this.attempt = 0;
    }
}
