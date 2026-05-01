-- V6: 评价相关表
CREATE TABLE `review` (
    `id`              BIGINT        NOT NULL,
    `order_id`        BIGINT        NOT NULL,
    `reviewer_id`     BIGINT        NOT NULL,
    `reviewee_id`     BIGINT        NOT NULL,
    `reviewer_role`   TINYINT       NOT NULL COMMENT '1=parent,2=tutor',
    `rating`          TINYINT       NOT NULL COMMENT '1-5 stars',
    `content`         VARCHAR(500)  DEFAULT NULL,
    `tags`            JSON          DEFAULT NULL COMMENT '["patient","punctual"]',
    `is_anonymous`    TINYINT       NOT NULL DEFAULT 0,
    `is_deleted`      TINYINT       NOT NULL DEFAULT 0,
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_reviewer` (`order_id`, `reviewer_id`),
    KEY `idx_reviewee` (`reviewee_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
