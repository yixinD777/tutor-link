<template>
  <view class="page">
    <view class="login-container">
      <!-- Brand — blue title on white -->
      <text class="brand-title">家教直连</text>
      <text class="brand-sub">打破中介信息费，直连大学生家教</text>

      <!-- Login / Register tabs -->
      <view class="tab-bar">
        <view :class="['tab', { active: mode === 'login' }]" @tap="mode = 'login'">登录</view>
        <view :class="['tab', { active: mode === 'register' }]" @tap="mode = 'register'">注册</view>
      </view>

      <!-- Form -->
      <view class="form">
        <!-- Role selector (register only) -->
        <view class="role-selector" v-if="mode === 'register'">
          <view :class="['role-option', { active: role === 1 }]" @tap="role = 1">
            <text class="role-icon">👨‍👩‍👧</text>
            <text class="role-label">我是家长</text>
            <text class="role-desc">发布需求，找家教</text>
          </view>
          <view :class="['role-option', { active: role === 2 }]" @tap="role = 2">
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
        <TlButton type="primary" @tap="handleSubmit">{{ mode === 'login' ? '登录' : '注册' }}</TlButton>
      </view>

      <!-- WeChat login -->
      <view class="divider">
        <view class="line" />
        <text class="divider-text">其他登录方式</text>
        <view class="line" />
      </view>
      <TlButton type="wechat" @tap="wxLogin">微信一键登录</TlButton>
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
const role = ref(1)

const accountInput = ref(null)
const passwordInput = ref(null)
const nicknameInput = ref(null)

function focusInput(name) {
  const refs = { account: accountInput, password: passwordInput, nickname: nicknameInput }
  const el = refs[name].value
  if (el && el.$el) {
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

<style lang="scss" scoped>
.page {
  background: $color-bg-page;
  min-height: 100vh;
  padding: 0 $spacing-page;
}

.login-container {
  width: 100%;
  text-align: center;
  padding-top: 160rpx;
}

.brand-title {
  font-size: $font-size-display;
  font-weight: $font-weight-bold;
  color: $color-primary;
  display: block;
  margin-bottom: $spacing-sm;
  letter-spacing: 2rpx;
}

.brand-sub {
  font-size: $font-size-base;
  font-weight: $font-weight-light;
  color: $color-text-secondary;
  display: block;
  margin-bottom: $spacing-4xl;
}

.tab-bar {
  display: flex;
  justify-content: center;
  margin-bottom: $spacing-2xl;
  gap: 60rpx;
}

.tab {
  font-size: $font-size-md;
  color: $color-text-placeholder;
  padding-bottom: $spacing-sm;
  border-bottom: 4rpx solid transparent;
  transition: all $duration-normal $ease-default;

  &.active {
    color: $color-primary;
    border-bottom-color: $color-primary;
    font-weight: $font-weight-bold;
  }
}

.form {
  text-align: left;
}

.form-input {
  width: 100%;
  margin-bottom: $spacing-lg;
  background: $color-bg-card;
  border: 2rpx solid $color-border;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  font-size: $font-size-base;
  box-sizing: border-box;
  color: $color-text-primary;
  transition: all $duration-normal $ease-default;

  &:focus-within {
    border-color: $color-primary;
    background: $color-bg-card;
  }

  input {
    color: $color-text-primary;
  }
}

.role-selector {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.role-option {
  flex: 1;
  text-align: center;
  padding: $spacing-lg $spacing-sm;
  background: $color-bg-card;
  border: 2rpx solid $color-border;
  border-radius: $radius-xl;
  transition: all $duration-normal $ease-default;

  &.active {
    border-color: $color-primary;
    background: $color-primary-light;
  }
}

.role-icon {
  font-size: 48rpx;
  display: block;
  margin-bottom: $spacing-xs;
}

.role-label {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  display: block;
}

.role-desc {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  display: block;
  margin-top: 4rpx;
  font-weight: $font-weight-light;
}

.divider {
  display: flex;
  align-items: center;
  margin: $spacing-3xl 0 $spacing-xl;
}

.line {
  flex: 1;
  height: 1rpx;
  background: $color-border;
}

.divider-text {
  padding: 0 $spacing-md;
  font-size: $font-size-sm;
  color: $color-text-placeholder;
  font-weight: $font-weight-light;
}
</style>
