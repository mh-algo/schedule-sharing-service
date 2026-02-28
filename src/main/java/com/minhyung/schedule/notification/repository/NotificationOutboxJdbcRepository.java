package com.minhyung.schedule.notification.repository;

import com.minhyung.schedule.notification.domain.NotificationCreateQueueMessage;
import com.minhyung.schedule.notification.domain.SendingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
public class NotificationOutboxJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public NotificationOutboxJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void batchInsert(List<NotificationCreateQueueMessage> rows) {
        // notifications batch insert
        String notificationSql = """
                    INSERT INTO notifications(receiver_id, target_type, target_id, payload, is_read)
                    VALUES (?, ?, ?, ?, 0)
                """;

        jdbcTemplate.batchUpdate(notificationSql, rows, rows.size(), ((ps, row) -> {
            ps.setLong(1, row.receiverId());
            ps.setByte(2, row.targetType().getCode());
            ps.setLong(3, row.targetId());
            ps.setString(4, row.payload());
        }));

        // notification_outbox batch insert
        String notificationOutboxSql = """
                INSERT INTO notification_outbox(receiver_id, payload, status, attempt)
                VALUES (?, ?, ?, 0)
            """;

        jdbcTemplate.batchUpdate(notificationOutboxSql, rows, rows.size(), ((ps, row) -> {
            ps.setLong(1, row.receiverId());
            ps.setString(2, row.payload());
            ps.setByte(3, SendingStatus.READY.getCode());
        }));
    }
}
