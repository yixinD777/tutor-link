-- V13: 排期确认流程 - 新增待确认状态和创建人字段

-- Step 1: 添加 created_by_user_id 列（先允许为空）
ALTER TABLE course_schedule
    ADD COLUMN created_by_user_id BIGINT DEFAULT NULL COMMENT '创建排期的用户ID' AFTER tutor_user_id;

-- Step 2: 回填历史数据（历史排期都是家长创建的）
UPDATE course_schedule SET created_by_user_id = parent_user_id WHERE created_by_user_id IS NULL;

-- Step 3: 改为 NOT NULL
ALTER TABLE course_schedule MODIFY COLUMN created_by_user_id BIGINT NOT NULL COMMENT '创建排期的用户ID';

-- Step 4: 排期状态新增待确认(0)，默认值改为0
ALTER TABLE course_schedule
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '0=待确认 1=生效 2=暂停 3=已结束';

-- Step 5: 索引
ALTER TABLE course_schedule ADD INDEX idx_created_by (created_by_user_id);
