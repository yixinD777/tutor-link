-- V7: 系统通知表
CREATE TABLE `system_notification` (
    `id`              BIGINT        NOT NULL,
    `user_id`         BIGINT        NOT NULL,
    `type`            TINYINT       NOT NULL COMMENT '1=order,2=payment,3=certification,4=system',
    `title`           VARCHAR(100)  NOT NULL,
    `content`         VARCHAR(500)  NOT NULL,
    `related_id`      BIGINT        DEFAULT NULL,
    `is_read`         TINYINT       NOT NULL DEFAULT 0,
    `is_deleted`      TINYINT       NOT NULL DEFAULT 0,
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知';
