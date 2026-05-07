/**
 * Tutor-Link 常量定义
 */

/** 星期文本 (1=周一, 7=周日) */
export const WEEK_DAYS = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

/** 星期文本 (0-indexed, 用于 picker) */
export const WEEK_DAYS_PICKER = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

/** 订单状态 → 状态主题色映射 */
export const ORDER_STATUS_THEME = {
  1: 'pending',   // 待确认
  11: 'pending',  // 有意向
  2: 'info',      // 待支付
  10: 'info',     // 试课中
  3: 'active',    // 已支付
  4: 'active',    // 进行中
  5: 'neutral',   // 已完成
  6: 'error',     // 已取消
  7: 'pending',   // 退款中
  8: 'neutral',   // 已退款
  9: 'error',     // 争议中
}

/** 排期状态 → 状态主题色映射 */
export const SCHEDULE_STATUS_THEME = {
  0: 'pending',  // 待确认
  1: 'active',   // 生效
  2: 'pending',  // 暂停
  3: 'neutral',  // 已结束
}

/** 课时状态 → 状态主题色映射 */
export const SESSION_STATUS_THEME = {
  1: 'info',     // 待上课
  2: 'active',   // 进行中
  3: 'neutral',  // 已完成
  4: 'error',    // 缺席
  5: 'error',    // 已取消
}

/** 试课状态 → 状态主题色映射 */
export const TRIAL_STATUS_THEME = {
  1: 'pending',  // 待确认
  2: 'active',   // 已确认
  3: 'neutral',  // 已完成
  4: 'error',    // 已取消
}

/** 认证状态 → 状态主题色映射 */
export const CERT_STATUS_THEME = {
  0: 'neutral',  // 未认证
  1: 'pending',  // 审核中
  2: 'active',   // 已认证
  3: 'error',    // 未通过
}

/** 周课表网格常量 */
export const SLOT_HEIGHT = 80   // rpx per hour slot
export const HOUR_START = 7     // 最早显示时间
export const HOUR_END = 23      // 最晚显示时间
