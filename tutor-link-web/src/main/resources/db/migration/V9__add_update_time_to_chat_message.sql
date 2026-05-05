-- V9: chat_message 表缺少 update_time 列
ALTER TABLE `chat_message` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`;
