package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.SendingStatus;
import com.minhyung.schedule.notification.domain.entity.NotificationOutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxEntity, Long> {
    @Query(value = """
        select no
        from NotificationOutboxEntity no
        where no.status in :statuses
            and (no.leaseUntil is null or no.leaseUntil < CURRENT_TIMESTAMP)
        order by no.id
    """)
    List<NotificationOutboxEntity> findReadyForClaim(@Param("statuses") Collection<SendingStatus> statuses, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value = """
        update NotificationOutboxEntity no
        set no.status = com.minhyung.schedule.notification.domain.SendingStatus.PROGRESSING,
            no.leaseUntil = :leaseUntil,
            no.attempt = no.attempt + 1,
            no.lastAttemptAt = CURRENT_TIMESTAMP,
            no.updatedAt = CURRENT_TIMESTAMP
        where no.id in :ids
    """)
    int updateProgressing(@Param("ids") Collection<Long> ids,
                          @Param("leaseUntil") LocalDateTime leaseUntil);
}
