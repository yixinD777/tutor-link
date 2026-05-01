-- V2: 认证与字典表
CREATE TABLE `student_certification` (
    `id`                      BIGINT        NOT NULL,
    `user_id`                 BIGINT        NOT NULL,
    `real_name`               VARCHAR(50)   NOT NULL,
    `id_card_no`              VARCHAR(64)   DEFAULT NULL COMMENT 'AES encrypted',
    `student_id_no`           VARCHAR(50)   DEFAULT NULL,
    `student_id_photo_front`  VARCHAR(512)  NOT NULL,
    `student_id_photo_back`   VARCHAR(512)  DEFAULT NULL,
    `handheld_photo`          VARCHAR(512)  DEFAULT NULL,
    `ocr_result`              JSON          DEFAULT NULL,
    `status`                  TINYINT       NOT NULL DEFAULT 1 COMMENT '1=pending,2=approved,3=rejected',
    `reject_reason`           VARCHAR(200)  DEFAULT NULL,
    `reviewer_id`             BIGINT        DEFAULT NULL,
    `review_time`             DATETIME      DEFAULT NULL,
    `is_deleted`              TINYINT       NOT NULL DEFAULT 0,
    `create_time`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生认证记录';

CREATE TABLE `subject` (
    `id`              BIGINT       NOT NULL,
    `name`            VARCHAR(30)  NOT NULL,
    `parent_id`       BIGINT       DEFAULT 0 COMMENT '0=top-level',
    `sort_order`      INT          NOT NULL DEFAULT 0,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科目字典';

CREATE TABLE `tutor_subject` (
    `id`              BIGINT       NOT NULL,
    `tutor_user_id`   BIGINT       NOT NULL,
    `subject_id`      BIGINT       NOT NULL,
    `grade_range`     VARCHAR(50)  DEFAULT NULL COMMENT 'e.g. "G1-G6,G7-G9"',
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tutor_subject` (`tutor_user_id`, `subject_id`),
    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家教-科目关联';

CREATE TABLE `area` (
    `id`              BIGINT       NOT NULL,
    `name`            VARCHAR(30)  NOT NULL,
    `parent_id`       BIGINT       DEFAULT 0,
    `level`           TINYINT      NOT NULL COMMENT '1=province,2=city,3=district',
    `sort_order`      INT          NOT NULL DEFAULT 0,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地区字典';
