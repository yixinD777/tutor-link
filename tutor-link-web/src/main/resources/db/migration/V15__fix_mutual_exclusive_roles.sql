-- V9: 角色互斥 — 清理组合角色数据
-- 规则：用户只能是 PARENT(1)、TUTOR(2) 或 ADMIN(4)，不能兼有

-- role=3 (PARENT+TUTOR): 有已认证家教档案 → 保留 TUTOR(2)
UPDATE user u
  INNER JOIN tutor_profile tp ON tp.user_id = u.id AND tp.certification_status > 0
SET u.role = 2
WHERE u.role = 3;

-- role=3 (PARENT+TUTOR): 剩余的 → 保留 PARENT(1)，清理其家教相关数据
DELETE ts FROM tutor_subject ts
  INNER JOIN tutor_profile tp ON tp.id = ts.tutor_user_id
  INNER JOIN user u ON u.id = tp.user_id AND u.role = 3;
DELETE FROM tutor_profile WHERE user_id IN (SELECT id FROM user WHERE role = 3);
UPDATE user SET role = 1 WHERE role = 3;

-- role=5 (PARENT+ADMIN) → ADMIN(4)
UPDATE user SET role = 4 WHERE role = 5;

-- role=6 (TUTOR+ADMIN) → ADMIN(4)
UPDATE user SET role = 4 WHERE role = 6;

-- role=7 (PARENT+TUTOR+ADMIN) → ADMIN(4)
UPDATE user SET role = 4 WHERE role = 7;
