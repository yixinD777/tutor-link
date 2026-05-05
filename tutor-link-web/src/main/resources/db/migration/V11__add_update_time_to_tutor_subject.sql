-- V11: tutor_subject 表缺少 update_time 列
ALTER TABLE `tutor_subject` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`;
