import request from './request'

export function getPayments(params) {
  return request.get('/admin/payments', { params })
}

export function getPaymentDetail(id) {
  return request.get(`/admin/payments/${id}`)
}

export function getRefunds(params) {
  return request.get('/admin/refunds', { params })
}

export function getRefundDetail(id) {
  return request.get(`/admin/refunds/${id}`)
}
