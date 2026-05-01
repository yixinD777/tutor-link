import { post, get } from './request'

// 微信登录
export function wxLogin(code, encryptedData, iv) {
  return post('/auth/wx-login', { code, encryptedData, iv })
}

// 手机号登录
export function phoneLogin(phone, smsCode) {
  return post('/auth/phone-login', { phone, smsCode })
}

// 发送验证码
export function sendSmsCode(phone) {
  return post('/auth/sms-code', { phone })
}

// 刷新 Token
export function refreshToken(token) {
  return post('/auth/refresh?refreshToken=' + token)
}

// 退出登录
export function logout() {
  return post('/auth/logout')
}
