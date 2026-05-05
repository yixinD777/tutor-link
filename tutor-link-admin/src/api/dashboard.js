import request from './request'

export function getStats() {
  return request.get('/admin/dashboard/stats')
}

export function getDetailedStats() {
  return request.get('/admin/dashboard/detailed-stats')
}
