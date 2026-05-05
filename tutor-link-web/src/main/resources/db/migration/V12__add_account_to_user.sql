-- V12: user 表新增 account 列（账号密码登录）
SET @dbname = DATABASE();
SET @tablename = 'user';
SET @columnname = 'account';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE `user` ADD COLUMN `account` VARCHAR(50) DEFAULT NULL UNIQUE AFTER `id`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
