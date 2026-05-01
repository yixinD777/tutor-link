import { post, get } from './request'

// 创建评价
export function createReview(data) {
  return post('/reviews?role=' + (data.role || 0), data)
}

// 获取家教评价列表
export function listTutorReviews(tutorUserId) {
  return get(`/reviews/tutor/${tutorUserId}`)
}

// 获取订单评价
export function getOrderReviews(orderId) {
  return get(`/reviews/order/${orderId}`)
}
