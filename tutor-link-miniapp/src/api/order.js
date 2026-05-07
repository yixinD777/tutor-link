import { post, put, get } from './request'

// 创建订单
export function createOrder(data) {
  return post('/orders', data)
}

// 我的订单列表
export function listMyOrders(params) {
  return get('/orders', params)
}

// 获取订单详情
export function getOrder(id) {
  return get(`/orders/${id}`)
}

// 订单状态流转日志
export function getOrderLogs(id) {
  return get(`/orders/${id}/logs`)
}

// 待接单订单 (家教)
export function listPendingOrders(params) {
  return get('/orders/pending', params)
}

// 待接单订单 Feed 流 (游标分页)
export function listPendingOrdersFeed(params) {
  return get('/orders/pending/feed', params)
}

// 家教表达意向
export function expressInterest(id) {
  return put(`/orders/${id}/interest`)
}

// 家长确认委托
export function confirmDelegation(id) {
  return put(`/orders/${id}/confirm-delegation`)
}

// 取消订单
export function cancelOrder(id, data) {
  return put(`/orders/${id}/cancel?role=${data.role || 0}`, { cancelReason: data.cancelReason })
}

// 开始上课
export function startOrder(id) {
  return put(`/orders/${id}/start`)
}

// 确认完成
export function completeOrder(id) {
  return put(`/orders/${id}/complete`)
}

// 支付预下单（开发环境自动支付成功）
export function prepay(orderId) {
  return post(`/payments/prepay?orderId=${orderId}`)
}
