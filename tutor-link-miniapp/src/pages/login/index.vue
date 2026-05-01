<template>
  <view class="page">
    <view class="login-container">
      <text class="title">家教直连</text>
      <text class="subtitle">打破中介信息费，直连大学生家教</text>

      <!-- 微信一键登录 -->
      <button class="wx-login-btn" @tap="wxLogin">
        微信一键登录
      </button>

      <!-- 手机号登录 -->
      <view class="phone-login">
        <view class="input-group">
          <input class="input" v-model="phone" placeholder="请输入手机号" type="number" maxlength="11" />
        </view>
        <view class="input-group">
          <input class="input code-input" v-model="smsCode" placeholder="验证码" type="number" maxlength="6" />
          <button class="code-btn" @tap="sendCode" :disabled="countdown > 0">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>
        <button class="phone-login-btn" @tap="phoneLogin">登录</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { wxLogin as wxLoginApi, phoneLogin as phoneLoginApi, sendSmsCode } from '../../api/auth'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const phone = ref('')
const smsCode = ref('')
const countdown = ref(0)

async function wxLogin() {
  try {
    const loginRes = await new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
    const data = await wxLoginApi(loginRes.code)
    userStore.setLoginInfo(data)
    uni.switchTab({ url: '/pages/index/index' })
  } catch (e) {
    uni.showToast({ title: '微信登录失败', icon: 'none' })
  }
}

async function sendCode() {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    await sendSmsCode(phone.value)
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
  } catch (e) {
    console.error('Send code failed', e)
  }
}

async function phoneLogin() {
  if (!phone.value || !smsCode.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  try {
    const data = await phoneLoginApi(phone.value, smsCode.value)
    userStore.setLoginInfo(data)
    uni.switchTab({ url: '/pages/index/index' })
  } catch (e) {
    console.error('Login failed', e)
  }
}
</script>

<style scoped>
.page { display: flex; justify-content: center; align-items: center; min-height: 100vh; padding: 40rpx; }
.login-container { width: 100%; text-align: center; }
.title { font-size: 56rpx; font-weight: bold; color: #4A90D9; display: block; margin-bottom: 16rpx; }
.subtitle { font-size: 28rpx; color: #999; display: block; margin-bottom: 80rpx; }
.wx-login-btn { background: #07C160; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; margin-bottom: 60rpx; }
.phone-login { text-align: left; }
.input-group { display: flex; align-items: center; margin-bottom: 24rpx; }
.input { flex: 1; background: #fff; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; }
.code-input { flex: 1; }
.code-btn { width: 200rpx; height: 80rpx; line-height: 80rpx; font-size: 24rpx; color: #4A90D9; background: #fff; border: none; margin-left: 20rpx; }
.phone-login-btn { background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; margin-top: 20rpx; }
</style>
