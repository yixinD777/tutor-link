-- V3: 订单相关表
CREATE TABLE `order_main` (
    `id`                BIGINT        NOT NULL COMMENT 'Snowflake order ID',
    `order_no`          VARCHAR(32)   NOT NULL COMMENT 'Display order number',
    `parent_user_id`    BIGINT        NOT NULL,
    `tutor_user_id`     BIGINT        DEFAULT NULL,
    `subject_id`        BIGINT        NOT NULL,
    `grade`             VARCHAR(20)   NOT NULL,
    `title`             VARCHAR(100)  NOT NULL,
    `description`       VARCHAR(1000) DEFAULT NULL,
    `teaching_address`  VARCHAR(300)  DEFAULT NULL,
    `longitude`         DECIMAL(10,7) DEFAULT NULL,
    `latitude`          DECIMAL(10,7) DEFAULT NULL,
    `teaching_mode`     TINYINT       NOT NULL DEFAULT 1 COMMENT '1=offline,2=online,3=both',
    `schedule`          JSON          DEFAULT NULL,
    `hourly_rate`       INT           NOT NULL COMMENT 'In cents',
    `total_hours`       INT           NOT NULL DEFAULT 0,
    `total_amount`      INT           NOT NULL COMMENT 'In cents',
    `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '1=PENDING,2=CONFIRMED,3=PAID,4=IN_PROGRESS,5=COMPLETED,6=CANCELLED,7=REFUNDING,8=REFUNDED,9=DISPUTED',
    `cancel_reason`     VARCHAR(200)  DEFAULT NULL,
    `cancel_by`         BIGINT        DEFAULT NULL,
    `confirm_time`      DATETIME      DEFAULT NULL,
    `start_time`        DATETIME      DEFAULT NULL,
    `complete_time`     DATETIME      DEFAULT NULL,
    `is_deleted`        TINYINT       NOT NULL DEFAULT 0,
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_parent` (`parent_user_id`, `status`),
    KEY `idx_tutor` (`tutor_user_id`, `status`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE `order_log` (
    `id`              BIGINT       NOT NULL,
    `order_id`        BIGINT       NOT NULL,
    `from_status`     TINYINT      DEFAULT NULL,
    `to_status`       TINYINT      NOT NULL,
    `operator_id`     BIGINT       DEFAULT NULL,
    `operator_type`   TINYINT      NOT NULL COMMENT '1=parent,2=tutor,3=system,4=admin',
    `action`          VARCHAR(50)  NOT NULL,
    `remark`          VARCHAR(300) DEFAULT NULL,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态流转日志';

CREATE TABLE `outbox_message` (
    `id`              BIGINT       NOT NULL,
    `topic`           VARCHAR(100) NOT NULL,
    `routing_key`     VARCHAR(100) NOT NULL,
    `payload`         TEXT         NOT NULL,
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0=pending,1=sent,2=failed',
    `retry_count`     INT          NOT NULL DEFAULT 0,
    `max_retries`     INT          NOT NULL DEFAULT 5,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事务发件箱';
