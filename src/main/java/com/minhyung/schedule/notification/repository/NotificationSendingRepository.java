package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.entity.NotificationSendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationSendingRepository extends JpaRepository<NotificationSendingEntity, Long> {
    // status = 1: READY, 5: RETRY_PENDING
//    @Query(value = """
//        select id
//        from notification_sending
//        where (status = 1 or status = 5)
//            and (next_push_at is null or next_push_at < now())
//        limit 1
//        for update skip locked
//    """, nativeQuery = true)
//    Optional<Long> findIdForUpdateSkipLocked();

    // status = 1: READY
    @Modifying
    @Query(value = """
        update notification_sending
        set status = 1,
            updated_at = now()
        where id = :id
    """, nativeQuery = true)
    int updateReadyById(@Param(("id")) Long id);

    // status = 1: READY, 2: PROGRESSING, 5: RETRY_PENDING
    @Modifying
    @Query(value = """
        update notification_sending
        set status = 2,
            lease_until = adddate(now(), interval :leaseSec second),
            attempt = attempt + 1,
            last_attempt_at = now(),
            updated_at = now()
        where id = :id
            and (status = 1 or status = 5)
            and lease_until is null
    """, nativeQuery = true)
    int updateProgressing(@Param("id") Long id, @Param("leaseSec") int leaseSec);

    // status = 2: PROGRESSING, 3: SENT
    @Modifying
    @Query(value = """
        update notification_sending
        set status = 3,
            sent_at = now(),
            lease_until = null,
            updated_at = now()
        where id = :id and status = 2
    """, nativeQuery = true)
    int updateSent(@Param("id") Long id);
}
