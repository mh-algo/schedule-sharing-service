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
@Table(name = "notification_sending")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NotificationSendingEntity extends CreatedAndUpdated {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private NotificationEntity notification;

    @Convert(converter = SendingStatusConverter.class)
    private SendingStatus status;

    private Integer attempt;

    @Column(name = "next_push_at")
    private LocalDateTime nextPushAt;

    @Builder
    public NotificationSendingEntity(NotificationEntity notification, SendingStatus status) {
        this.notification = notification;
        this.status = status;
        this.attempt = 0;
    }
}
