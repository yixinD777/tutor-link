import request from './request'

export function login(data) {
  return request.post('/auth/password-login', data)
}

export function logout() {
  return request.post('/auth/logout')
}
