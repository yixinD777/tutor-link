<template>
  <view class="page">
    <view class="login-container">
      <text class="title">家教直连</text>
      <text class="subtitle">打破中介信息费，直连大学生家教</text>

      <!-- 登录/注册切换 -->
      <view class="tab-bar">
        <view class="tab" :class="{ active: mode === 'login' }" @tap="mode = 'login'">登录</view>
        <view class="tab" :class="{ active: mode === 'register' }" @tap="mode = 'register'">注册</view>
      </view>

      <!-- 账号密码表单 -->
      <view class="form">
        <!-- 角色选择（仅注册时显示） -->
        <view class="role-selector" v-if="mode === 'register'">
          <view class="role-option" :class="{ active: role === 1 }" @tap="role = 1">
            <text class="role-icon">👨‍👩‍👧</text>
            <text class="role-label">我是家长</text>
            <text class="role-desc">发布需求，找家教</text>
          </view>
          <view class="role-option" :class="{ active: role === 2 }" @tap="role = 2">
            <text class="role-icon">🎓</text>
            <text class="role-label">我是学生</text>
            <text class="role-desc">接单授课，赚收入</text>
          </view>
        </view>

        <view class="form-input" @tap="focusInput('account')">
          <input ref="accountInput" v-model="account" placeholder="请输入账号" maxlength="20" />
        </view>
        <view class="form-input" @tap="focusInput('password')">
          <input ref="passwordInput" v-model="password" placeholder="请输入密码" :password="true" maxlength="20" />
        </view>
        <view class="form-input" v-if="mode === 'register'" @tap="focusInput('nickname')">
          <input ref="nicknameInput" v-model="nickname" placeholder="昵称（选填，默认为账号）" maxlength="20" />
        </view>
        <button class="submit-btn" @tap="handleSubmit">
          {{ mode === 'login' ? '登录' : '注册' }}
        </button>
      </view>

      <!-- 微信登录 -->
      <view class="divider">
        <view class="line"></view>
        <text class="divider-text">其他登录方式</text>
        <view class="line"></view>
      </view>
      <button class="wx-login-btn" @tap="wxLogin">微信一键登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { register as registerApi, passwordLogin as passwordLoginApi, wxLogin as wxLoginApi } from '../../api/auth'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const mode = ref('login')
const account = ref('')
const password = ref('')
const nickname = ref('')
const role = ref(1) // 1=家长, 2=学生

const accountInput = ref(null)
const passwordInput = ref(null)
const nicknameInput = ref(null)

function focusInput(name) {
  const refs = { account: accountInput, password: passwordInput, nickname: nicknameInput }
  const el = refs[name].value
  if (el && el.$el) {
    // uni-app 组件：找到内部真实 input
    const realInput = el.$el.querySelector('input')
    if (realInput) realInput.focus()
  } else if (el) {
    el.focus()
  }
}

async function handleSubmit() {
  if (!account.value || !password.value) {
    uni.showToast({ title: '请填写账号和密码', icon: 'none' })
    return
  }
  if (mode.value === 'register') {
    await doRegister()
  } else {
    await doLogin()
  }
}

async function doRegister() {
  try {
    const data = await registerApi(account.value, password.value, nickname.value || undefined, role.value)
    userStore.setLoginInfo(data)
    uni.switchTab({ url: '/pages/index/index' })
  } catch (e) {
    console.error('Register failed', e)
  }
}

async function doLogin() {
  try {
    const data = await passwordLoginApi(account.value, password.value)
    userStore.setLoginInfo(data)
    uni.switchTab({ url: '/pages/index/index' })
  } catch (e) {
    console.error('Login failed', e)
  }
}

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
</script>

<style scoped>
.page { padding: 120rpx 40rpx 40rpx; background: #f5f5f5; min-height: 100vh; }
.login-container { width: 100%; text-align: center; }
.title { font-size: 56rpx; font-weight: bold; color: #4A90D9; display: block; margin-bottom: 16rpx; }
.subtitle { font-size: 28rpx; color: #999; display: block; margin-bottom: 60rpx; }

.tab-bar { display: flex; justify-content: center; margin-bottom: 40rpx; gap: 60rpx; }
.tab { font-size: 32rpx; color: #999; padding-bottom: 12rpx; border-bottom: 4rpx solid transparent; }
.tab.active { color: #4A90D9; border-bottom-color: #4A90D9; font-weight: bold; }

.form { text-align: left; }
.form-input {
  width: 100%;
  margin-bottom: 24rpx;
  background: #fff;
  border: 2rpx solid #ddd;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  color: #333;
  cursor: text;
}
.form-input:focus-within {
  border-color: #4A90D9;
}
.form-input input,
.form-input :deep(uni-input) {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 28rpx;
  color: #333;
}
.submit-btn { background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; margin-top: 20rpx; }

.role-selector { display: flex; gap: 20rpx; margin-bottom: 30rpx; }
.role-option {
  flex: 1; text-align: center; padding: 24rpx 16rpx;
  background: #fff; border: 2rpx solid #ddd; border-radius: 16rpx;
  transition: all 0.2s;
}
.role-option.active { border-color: #4A90D9; background: #EBF3FB; }
.role-icon { font-size: 48rpx; display: block; margin-bottom: 8rpx; }
.role-label { font-size: 28rpx; font-weight: bold; color: #333; display: block; }
.role-desc { font-size: 22rpx; color: #999; display: block; margin-top: 4rpx; }

.divider { display: flex; align-items: center; margin: 50rpx 0 30rpx; }
.line { flex: 1; height: 1rpx; background: #ddd; }
.divider-text { padding: 0 20rpx; font-size: 24rpx; color: #999; }

.wx-login-btn { background: #07C160; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; }

</style>
