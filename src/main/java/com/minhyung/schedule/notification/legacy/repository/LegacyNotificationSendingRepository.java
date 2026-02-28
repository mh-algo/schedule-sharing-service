package com.minhyung.schedule.notification.legacy.repository;

import com.minhyung.schedule.notification.legacy.domain.RetryInfo;
import com.minhyung.schedule.notification.domain.entity.NotificationSendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Deprecated(forRemoval = true)
public interface LegacyNotificationSendingRepository extends JpaRepository<NotificationSendingEntity, Long> {
    /*
        status = 0: PENDING, 1: READY, 2: PROGRESSING, 3: SENT, 4: FAILED, 5: RETRY_PENDING
    */

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 1,
            updated_at = now()
        where id = :id
    """, nativeQuery = true)
    int updateReadyById(@Param(("id")) Long id);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 2,
            lease_until = date_add(now(), interval :leaseSec second),
            attempt = :attempt + 1,
            last_attempt_at = now(),
            updated_at = now()
        where id = :id
            and (status = 1 or status = 5)
            and lease_until is null
    """, nativeQuery = true)
    int updateProgressing(@Param("id") Long id, @Param("leaseSec") int leaseSec, @Param("attempt") int attempt);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 3,
            sent_at = now(),
            lease_until = null,
            updated_at = now()
        where id = :id
          and status = 2
    """, nativeQuery = true)
    int updateSent(@Param("id") Long id);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 4,
            lease_until = null,
            next_push_at = null,
            updated_at = now()
        where id = :id
          and status = 2

    """, nativeQuery = true)
    int updateFailed(@Param("id") Long id);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 5,
            lease_until = null,
            last_attempt_at = now(),
            last_error = :error,
            next_push_at = date_add(now(), interval :backoffSec second),
            updated_at = now()
        where id = :id
    """, nativeQuery = true)
    int updateRetryPending(@Param(("id")) Long id, @Param("error") String error, @Param("backoffSec") long backoffSec);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 5,
            lease_until = null,
            attempt = :attempt + 1,
            last_attempt_at = now(),
            last_error = :error,
            next_push_at = date_add(now(), interval :backoffSec second),
            updated_at = now()
        where id = :id
    """, nativeQuery = true)
    int updateRetryPending(@Param(("id")) Long id, @Param("error") String error, @Param("backoffSec") long backoffSec, @Param("attempt") int attempt);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 5,
            lease_until = null,
            updated_at = now()
        where status = 2
          and lease_until < now()
        limit :batchSize
    """, nativeQuery = true)
    int updateProgressingExpiredToRetryPending(@Param("batchSize") int batchSize);

    // NULL 정렬 기준은 mysql 기준
    @Query(value = """
        select id as sendingId, notification_id, attempt
        from notification_sending
        where status = 5
          and (next_push_at is null or next_push_at <= now())
        order by next_push_at, id
        limit :batchSize
        for update skip locked
    """, nativeQuery = true)
    List<RetryInfo> findDueRetryIds(@Param("batchSize") int batchSize);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 1,
            lease_until = null,
            next_push_at = null,
            last_error = null,
            updated_at = now()
        where status = 5
          and id in (:retryIds)
    """, nativeQuery = true)
    int updateReadyByIds(@Param("retryIds") List<Long> retryIds);

    @Modifying
    @Query(value = """
        update notification_sending
        set status = 4,
            lease_until = null,
            next_push_at = null,
            updated_at = now()
        where status = 5
          and id in (:failIds)
    """, nativeQuery = true)
    int updateFailedByIds(@Param("failIds") List<Long> failIds);
}
