-- AI 用户长期记忆表
-- 每用户一行，通过 UNIQUE KEY uk_user_id 做 upsert
CREATE TABLE ai_user_memory (
    id          BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    summary     TEXT,
    facts       JSON,
    source_conv VARCHAR(64) COMMENT '触发本次更新的 conversationId',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_update_time (update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 客服用户长期记忆';
