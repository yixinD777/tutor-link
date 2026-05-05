import request from './request'

export function getUserTrend(params) {
  return request.get('/admin/analytics/user-trend', { params })
}

export function getOrderTrend(params) {
  return request.get('/admin/analytics/order-trend', { params })
}

export function getRevenueTrend(params) {
  return request.get('/admin/analytics/revenue-trend', { params })
}

export function getRefundTrend(params) {
  return request.get('/admin/analytics/refund-trend', { params })
}

export function getReviewRatingTrend(params) {
  return request.get('/admin/analytics/review-rating-trend', { params })
}

export function getOrderStatusDistribution(params) {
  return request.get('/admin/analytics/order-status-distribution', { params })
}

export function getSubjectPopularity(params) {
  return request.get('/admin/analytics/subject-popularity', { params })
}

export function getTeachingModeDistribution(params) {
  return request.get('/admin/analytics/teaching-mode-distribution', { params })
}

export function getGenderDistribution(params) {
  return request.get('/admin/analytics/gender-distribution', { params })
}

export function getRegionDistribution(params) {
  return request.get('/admin/analytics/region-distribution', { params })
}

export function getTutorRatingDistribution(params) {
  return request.get('/admin/analytics/tutor-rating-distribution', { params })
}

export function getUniversityDistribution(params) {
  return request.get('/admin/analytics/university-distribution', { params })
}

export function getCertificationStatusDistribution(params) {
  return request.get('/admin/analytics/certification-status-distribution', { params })
}

export function getHourlyRateDistribution(params) {
  return request.get('/admin/analytics/hourly-rate-distribution', { params })
}

export function getPaymentStatusDistribution(params) {
  return request.get('/admin/analytics/payment-status-distribution', { params })
}

export function getReviewRatingDistribution(params) {
  return request.get('/admin/analytics/review-rating-distribution', { params })
}
