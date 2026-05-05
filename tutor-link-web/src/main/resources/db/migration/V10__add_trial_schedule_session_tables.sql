-- V10: 试课、课程排期、单次课程履约表

-- 试课表
CREATE TABLE `trial_lesson` (
    `id`                BIGINT        NOT NULL,
    `order_id`          BIGINT        NOT NULL,
    `parent_user_id`    BIGINT        NOT NULL,
    `tutor_user_id`     BIGINT        NOT NULL,
    `trial_date`        DATETIME      NOT NULL COMMENT '试课日期时间',
    `trial_duration`    INT           NOT NULL DEFAULT 60 COMMENT '试课时长(分钟)',
    `trial_price`       INT           NOT NULL COMMENT '试课价格(分)',
    `trial_address`     VARCHAR(300)  DEFAULT NULL,
    `trial_mode`        TINYINT       NOT NULL DEFAULT 1 COMMENT '1=线下 2=线上',
    `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '1=待确认 2=已确认 3=已完成 4=已取消',
    `parent_feedback`   VARCHAR(500)  DEFAULT NULL,
    `result`            TINYINT       DEFAULT NULL COMMENT '1=通过 2=不通过',
    `is_deleted`        TINYINT       NOT NULL DEFAULT 0,
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_parent` (`parent_user_id`, `status`),
    KEY `idx_tutor` (`tutor_user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试课表';

-- 课程排期表
CREATE TABLE `course_schedule` (
    `id`                BIGINT        NOT NULL,
    `order_id`          BIGINT        NOT NULL,
    `parent_user_id`    BIGINT        NOT NULL,
    `tutor_user_id`     BIGINT        NOT NULL,
    `day_of_week`       TINYINT       NOT NULL COMMENT '1=周一 7=周日',
    `start_time`        TIME          NOT NULL COMMENT '上课时间',
    `end_time`          TIME          NOT NULL COMMENT '下课时间',
    `teaching_address`  VARCHAR(300)  DEFAULT NULL,
    `teaching_mode`     TINYINT       NOT NULL DEFAULT 1 COMMENT '1=线下 2=线上',
    `hourly_rate`       INT           NOT NULL COMMENT '单节课时费(分)',
    `effective_from`    DATE          NOT NULL COMMENT '生效日期',
    `effective_until`   DATE          DEFAULT NULL COMMENT '失效日期',
    `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '1=生效 2=暂停 3=已结束',
    `is_deleted`        TINYINT       NOT NULL DEFAULT 0,
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_parent` (`parent_user_id`, `status`),
    KEY `idx_tutor` (`tutor_user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程排期表';

-- 单次课程履约表
CREATE TABLE `lesson_session` (
    `id`                BIGINT        NOT NULL,
    `schedule_id`       BIGINT        NOT NULL,
    `order_id`          BIGINT        NOT NULL,
    `lesson_date`       DATE          NOT NULL COMMENT '上课日期',
    `start_time`        TIME          NOT NULL COMMENT '计划开始时间',
    `end_time`          TIME          NOT NULL COMMENT '计划结束时间',
    `actual_start`      TIME          DEFAULT NULL COMMENT '实际开始时间',
    `actual_end`        TIME          DEFAULT NULL COMMENT '实际结束时间',
    `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '1=待上课 2=进行中 3=已完成 4=缺席 5=已取消',
    `check_in_time`     DATETIME      DEFAULT NULL COMMENT '老师签到时间',
    `check_out_time`    DATETIME      DEFAULT NULL COMMENT '老师签退时间',
    `parent_confirm`    TINYINT       NOT NULL DEFAULT 0 COMMENT '0=未确认 1=已确认',
    `remark`            VARCHAR(500)  DEFAULT NULL,
    `is_deleted`        TINYINT       NOT NULL DEFAULT 0,
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_schedule` (`schedule_id`, `lesson_date`),
    KEY `idx_order` (`order_id`),
    KEY `idx_date_status` (`lesson_date`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单次课程履约表';
