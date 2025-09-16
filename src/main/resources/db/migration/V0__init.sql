CREATE TABLE `users` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `username`	VARCHAR(50) NOT NULL,
    `password`	VARCHAR(100)	NOT NULL,
    `status`    TINYINT NOT NULL    DEFAULT 1   COMMENT '0: SUSPENDED, 1: ACTIVE, 2: UNVERIFIED',
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    `updated_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP   ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`	TIMESTAMP	NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_username (`username`)
);

CREATE TABLE `groups` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `owner_id`	BIGINT	NOT NULL,
    `name`	VARCHAR(50)	NOT NULL,
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    `updated_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP   ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`	TIMESTAMP	NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_groups_owner_id FOREIGN KEY (`owner_id`) REFERENCES `users`(`id`)
);

CREATE TABLE `members` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `group_id`	BIGINT	NOT NULL,
    `user_id`	BIGINT	NOT NULL,
    `role_type`	TINYINT	NOT NULL	DEFAULT 3	COMMENT '0: 블랙리스트, 1: 그룹장, 2: 관리자, 3: 사용자',
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    `updated_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP   ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`	TIMESTAMP	NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_members_group_id FOREIGN KEY (`group_id`) REFERENCES `groups`(`id`),
    CONSTRAINT fk_members_user_id FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
);

CREATE TABLE `schedules` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `group_id`	BIGINT	NOT NULL,
    `title`	VARCHAR(50)	NOT NULL,
    `description`	TEXT	NOT NULL,
    `date`	TIMESTAMP	NOT NULL,
    `notification_date`	TIMESTAMP	NULL,
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    `updated_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP   ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`	TIMESTAMP	NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_schedules_group_id FOREIGN KEY (`group_id`) REFERENCES `groups`(`id`)
);

CREATE TABLE `schedule_members` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `schedule_id`	BIGINT	NOT NULL,
    `member_id`	BIGINT	NOT NULL,
    `attend`	TINYINT	NOT NULL	DEFAULT 1	COMMENT '0: DECLINED, 1: ACCEPTED',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_schedule_members_schedule_id FOREIGN KEY (`schedule_id`) REFERENCES `schedules`(`id`),
    CONSTRAINT fk_schedule_members_member_id FOREIGN KEY (`member_id`) REFERENCES `members`(`id`)
);

CREATE TABLE `group_invites` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `group_id`	BIGINT	NOT NULL,
    `inviter_id`	BIGINT	NOT NULL,
    `invitee_id`	BIGINT	NOT NULL,
    `status`	TINYINT	NOT NULL	DEFAULT 0	COMMENT '0: PENDING, 1: ACCEPTED, 2: DECLINED',
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    `responded_at`	TIMESTAMP	NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_group_invites_group_id FOREIGN KEY (`group_id`) REFERENCES `groups`(`id`),
    CONSTRAINT fk_group_invites_inviter_id FOREIGN KEY (`inviter_id`) REFERENCES `users`(`id`),
    CONSTRAINT fk_group_invites_invitee_id FOREIGN KEY (`invitee_id`) REFERENCES `users`(`id`)
);

CREATE TABLE `notifications` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `type`	TINYINT	NOT NULL,
    `title`	VARCHAR(50)	NOT NULL,
    `message`	VARCHAR(256)	NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `notification_receivers` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `notification_id`	BIGINT	NOT NULL,
    `receiver_id`	BIGINT	NOT NULL,
    `target_type`	TINYINT	NOT NULL,
    `target_id`	BIGINT	NOT NULL	COMMENT 'type에 해당하는 id',
    `payload`	TEXT	NULL,
    `is_read`	TINYINT	NOT NULL	DEFAULT 0	COMMENT '0: FALSE, 1: TRUE',
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    `read_at`   TIMESTAMP	NULL,
    `deleted_at`	TIMESTAMP	NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_notification_receivers_notification_id FOREIGN KEY (`notification_id`) REFERENCES `notifications`(`id`),
    CONSTRAINT fk_notification_receivers_receiver_id FOREIGN KEY (`receiver_id`) REFERENCES `users`(`id`),
    UNIQUE KEY uk_identify_notification (`receiver_id`, `target_type`, `target_id`)
);

CREATE TABLE `notification_sending` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `notification_receiver_id`	BIGINT	NOT NULL,
    `status`	TINYINT	NOT NULL    DEFAULT 0	COMMENT '0: PENDING, 1: IN_QUEUE, 2: IN_PROGRESS, 3: SENT, 4: FAILED,5: CANCELLED',
    `attempt`	INT	NOT NULL    DEFAULT 0,
    `next_push_at`	TIMESTAMP	NULL,
    `version`	INT	NOT NULL    DEFAULT 0,
    `updated_at`   TIMESTAMP	NULL    DEFAULT CURRENT_TIMESTAMP   ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_notification_sending_notification_receiver_id FOREIGN KEY (`notification_receiver_id`) REFERENCES `notification_receivers`(`id`),
    UNIQUE KEY uk_notification_receiver_id (`notification_receiver_id`)
);

CREATE TABLE `group_activity_logs` (
    `id`	BIGINT	NOT NULL AUTO_INCREMENT,
    `group_id`	BIGINT	NOT NULL,
    `actor_user_id`	BIGINT	NOT NULL,
    `action_type`	TINYINT	NOT NULL,
    `target_user_id`	BIGINT	NOT NULL,
    `target_schedule_id`	BIGINT	NOT NULL,
    `description`	VARCHAR(256)	NOT NULL,
    `created_at`	TIMESTAMP	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_group_activity_logs_group_id FOREIGN KEY (`group_id`) REFERENCES `groups`(`id`),
    CONSTRAINT fk_group_activity_logs_actor_user_id FOREIGN KEY (`actor_user_id`) REFERENCES `members`(`id`),
    CONSTRAINT fk_group_activity_logs_target_user_id FOREIGN KEY (`target_user_id`) REFERENCES `members`(`id`),
    CONSTRAINT fk_group_activity_logs_target_schedule_id FOREIGN KEY (`target_schedule_id`) REFERENCES `schedules`(`id`)
);