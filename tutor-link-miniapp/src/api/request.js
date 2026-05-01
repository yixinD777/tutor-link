const BASE_URL = 'http://localhost:8080/api/v1'

let accessToken = ''

export function setToken(token) {
  accessToken = token
  uni.setStorageSync('accessToken', token)
}

export function getToken() {
  if (!accessToken) {
    accessToken = uni.getStorageSync('accessToken') || ''
  }
  return accessToken
}

export function clearToken() {
  accessToken = ''
  uni.removeStorageSync('accessToken')
  uni.removeStorageSync('refreshToken')
}

export function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const header = {
      'Content-Type': 'application/json',
      ...(options.header || {})
    }
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: (res) => {
        if (res.statusCode === 200) {
          const data = res.data
          if (data.code === 200) {
            resolve(data.data)
          } else if (data.code === 401) {
            clearToken()
            uni.reLaunch({ url: '/pages/login/index' })
            reject(data)
          } else {
            uni.showToast({ title: data.message || '请求失败', icon: 'none' })
            reject(data)
          }
        } else {
          uni.showToast({ title: '网络错误', icon: 'none' })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export function get(url, data) {
  return request({ url, method: 'GET', data })
}

export function post(url, data) {
  return request({ url, method: 'POST', data })
}

export function put(url, data) {
  return request({ url, method: 'PUT', data })
}

export function del(url, data) {
  return request({ url, method: 'DELETE', data })
}

export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: BASE_URL + '/files/upload',
      filePath,
      name: 'file',
      header: token ? { 'Authorization': `Bearer ${token}` } : {},
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)
        } else {
          reject(data)
        }
      },
      fail: reject
    })
  })
}
