/**
 * Tutor-Link 格式化工具函数
 * 统一管理跨页面复用的格式化逻辑
 */

/** 价格格式化（分 → 元） */
export function formatRate(min, max) {
  if (!min && !max) return '价格面议'
  if (min && max) return `¥${min / 100}-${max / 100}/时`
  return `¥${(min || max) / 100}/时`
}

/** 单价格式化（分 → 元） */
export function formatPrice(cents) {
  if (cents == null) return '¥0'
  return `¥${(cents / 100).toFixed(2)}`
}

/** 授课模式文本 */
export function modeText(mode) {
  return { 1: '线下', 2: '线上', 3: '均可' }[mode] || '未知'
}

/** ISO 时间 → 可读时间 (YYYY-MM-DD HH:mm) */
export function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

/** ISO 时间 → 仅时间 (HH:mm) */
export function formatMsgTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(11, 16)
}

/** ISO 时间 → 智能相对时间（今天显示时间，否则显示月-日） */
export function formatSmartTime(t) {
  if (!t) return ''
  const d = t.replace('T', ' ')
  const now = new Date()
  const date = new Date(d)
  if (now.toDateString() === date.toDateString()) return d.substring(11, 16)
  return d.substring(5, 10)
}

/** 学历文本 */
export function formatEdu(level) {
  return { 1: '本科', 2: '硕士', 3: '博士' }[level] || '本科'
}

/** 评分星级文本 */
export function formatRating(rating) {
  if (!rating || rating === 0) return '暂无评分'
  const full = Math.floor(rating)
  return '★'.repeat(full) + '☆'.repeat(5 - full) + ` ${rating}`
}

// ---- 状态文本映射 ----

/** 订单状态文本 */
export function orderStatusText(s) {
  const map = {
    1: '待确认', 2: '待支付', 3: '已支付', 4: '进行中', 5: '已完成',
    6: '已取消', 7: '退款中', 8: '已退款', 9: '争议中', 10: '试课中', 11: '有意向'
  }
  return map[s] || '未知'
}

/** 订单状态描述 */
export function orderStatusDesc(s) {
  const map = {
    1: '等待家教回应', 11: '家教已表达意向，请确认委托', 2: '请尽快完成支付',
    3: '已支付，等待家教开始上课', 4: '课程进行中', 5: '课程已完成',
    6: '订单已取消', 7: '退款处理中', 8: '退款已完成',
    9: '存在争议，请等待处理', 10: '试课进行中，请等待家长评价'
  }
  return map[s] || ''
}

/** 排期状态文本 */
export function scheduleStatusText(s) {
  return { 0: '待确认', 1: '生效', 2: '暂停', 3: '已结束' }[s] || ''
}

/** 课时状态文本 */
export function sessionStatusText(s) {
  return { 1: '待上课', 2: '进行中', 3: '已完成', 4: '缺席', 5: '已取消' }[s] || '未知'
}

/** 试课状态文本 */
export function trialStatusText(s) {
  return { 1: '待确认', 2: '已确认', 3: '已完成', 4: '已取消' }[s] || '未知'
}

/** 认证状态文本 */
export function certStatusText(s) {
  return { 0: '未认证', 1: '审核中', 2: '已认证', 3: '未通过' }[s] || '未认证'
}

// ---- 时间辅助 ----

/** 时间字符串 → 小时浮点数 */
export function timeToHour(t, fallback = 8) {
  if (!t) return fallback
  const [h, m] = t.split(':')
  return parseInt(h) + parseInt(m || 0) / 60
}

/** 获取指定偏移周的周一日期 */
export function getMonday(offset = 0) {
  const now = new Date()
  const day = now.getDay()
  const diff = now.getDate() - day + (day === 0 ? -6 : 1) + offset * 7
  const monday = new Date(now.setDate(diff))
  monday.setHours(0, 0, 0, 0)
  return monday
}

/** 判断日期是否为今天 */
export function isToday(date) {
  const now = new Date()
  return date.getFullYear() === now.getFullYear()
    && date.getMonth() === now.getMonth()
    && date.getDate() === now.getDate()
}

/** 日期 → 星期几 (1=周一, 7=周日) */
export function dateGetDow(date) {
  const d = date.getDay()
  return d === 0 ? 7 : d
}
