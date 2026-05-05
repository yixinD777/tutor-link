-- V8: order_log 和 system_notification 表缺少 update_time 列
-- 但对应的实体继承了 MpBaseEntity 的 updateTime 字段，导致 INSERT 时 SQL 报错

ALTER TABLE `order_log` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`;

ALTER TABLE `system_notification` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`;
