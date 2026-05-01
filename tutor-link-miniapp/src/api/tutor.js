import { get } from './request'

// 搜索家教列表
export function searchTutors(params) {
  return get('/tutors', params)
}

// 获取家教详情
export function getTutorDetail(userId) {
  return get(`/tutors/${userId}`)
}

// 获取家教科目
export function getTutorSubjects(userId) {
  return get(`/tutors/${userId}/subjects`)
}
