-- V4: 聊天相关表
CREATE TABLE `chat_conversation` (
    `id`                  BIGINT       NOT NULL,
    `conversation_no`     VARCHAR(32)  NOT NULL,
    `user_a_id`           BIGINT       NOT NULL COMMENT 'Smaller user ID',
    `user_b_id`           BIGINT       NOT NULL COMMENT 'Larger user ID',
    `last_message_id`     BIGINT       DEFAULT NULL,
    `last_message_content` VARCHAR(500) DEFAULT NULL,
    `last_message_time`   DATETIME     DEFAULT NULL,
    `user_a_unread`       INT          NOT NULL DEFAULT 0,
    `user_b_unread`       INT          NOT NULL DEFAULT 0,
    `is_deleted`          TINYINT      NOT NULL DEFAULT 0,
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_no` (`conversation_no`),
    UNIQUE KEY `uk_users` (`user_a_id`, `user_b_id`),
    KEY `idx_user_a` (`user_a_id`, `user_a_unread`),
    KEY `idx_user_b` (`user_b_id`, `user_b_unread`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话';

CREATE TABLE `chat_message` (
    `id`              BIGINT        NOT NULL,
    `conversation_id` BIGINT        NOT NULL,
    `sender_id`       BIGINT        NOT NULL,
    `receiver_id`     BIGINT        NOT NULL,
    `msg_type`        TINYINT       NOT NULL DEFAULT 1 COMMENT '1=text,2=image,3=order_card',
    `content`         TEXT          NOT NULL,
    `is_read`         TINYINT       NOT NULL DEFAULT 0,
    `is_deleted`      TINYINT       NOT NULL DEFAULT 0,
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_conversation_time` (`conversation_id`, `create_time`),
    KEY `idx_receiver_unread` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息';
