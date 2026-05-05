import request from './request'

export function getCertifications(params) {
  return request.get('/admin/certifications', { params })
}

export function getCertificationDetail(id) {
  return request.get(`/admin/certifications/${id}`)
}

export function approveCertification(id) {
  return request.put(`/admin/certifications/${id}/approve`)
}

export function rejectCertification(id, reason) {
  return request.put(`/admin/certifications/${id}/reject`, null, { params: { reason } })
}
