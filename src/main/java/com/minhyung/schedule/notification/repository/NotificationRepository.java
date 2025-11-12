package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.NotificationRetryInfo;
import com.minhyung.schedule.notification.domain.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query(value = """
        select new com.minhyung.schedule.notification.domain.NotificationRetryInfo(n.id, n.receiver.id, n.payload)
        from NotificationEntity n
        where n.id in (:notificationIds)
    """)
    List<NotificationRetryInfo> findRetryInfoByIds(List<Long> notificationIds);
}
