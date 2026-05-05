import request from './request'

export function getReviews(params) {
  return request.get('/admin/reviews', { params })
}

export function getReviewDetail(id) {
  return request.get(`/admin/reviews/${id}`)
}
