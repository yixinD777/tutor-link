import { post, put, get } from './request'

// 创建排期
export function createSchedule(data) {
  return post('/schedules', data)
}

// 我的排期列表
export function listMySchedules() {
  return get('/schedules/my')
}

// 确认排期
export function confirmSchedule(id) {
  return put(`/schedules/${id}/confirm`)
}

// 拒绝排期
export function rejectSchedule(id) {
  return put(`/schedules/${id}/reject`)
}

// 查询订单的排期
export function listSchedulesByOrder(orderId) {
  return get(`/schedules/order/${orderId}`)
}

// 暂停排期
export function pauseSchedule(id) {
  return put(`/schedules/${id}/pause`)
}

// 恢复排期
export function resumeSchedule(id) {
  return put(`/schedules/${id}/resume`)
}

// 查询订单的课时列表
export function listSessionsByOrder(orderId) {
  return get(`/schedules/order/${orderId}/sessions`)
}

// 老师签到
export function checkInSession(sessionId) {
  return put(`/schedules/sessions/${sessionId}/check-in`)
}

// 老师签退
export function checkOutSession(sessionId) {
  return put(`/schedules/sessions/${sessionId}/check-out`)
}

// 家长确认课时
export function confirmSession(sessionId) {
  return put(`/schedules/sessions/${sessionId}/confirm`)
}
