<template>
  <view class="page">
    <!-- Premium Hero — blue bg + glow drift -->
    <view class="hero">
      <TlAvatar :src="userStore.avatarUrl" size="large" :name="userStore.nickname || '未登录'" />
      <text class="hero-nickname">{{ userStore.nickname || '未登录' }}</text>
      <view class="hero-stats" v-if="userStore.isLoggedIn">
        <view class="stat-item">
          <text class="stat-num">0</text>
          <text class="stat-label">订单</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item">
          <text class="stat-num">0</text>
          <text class="stat-label">评价</text>
        </view>
      </view>
      <view class="cert-badge" v-if="userStore.isTutor()">
        <TlStatusBadge :status="certStatus ?? 0" type="cert" />
      </view>
    </view>

    <!-- Menu list -->
    <view class="menu-list">
      <view class="menu-item" @tap="goCertification" v-if="userStore.isTutor()">
        <text class="menu-text">学生认证</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goTutorProfile" v-if="userStore.isTutor()">
        <text class="menu-text">家教档案</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goOrders">
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="handleLogout" v-if="userStore.isLoggedIn">
        <text class="menu-text logout">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goLogin" v-else>
        <text class="menu-text">登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { logout as logoutApi } from '../../api/auth'
import { get } from '../../api/request'

const userStore = useUserStore()
const certStatus = ref(null)

onMounted(async () => {
  userStore.loadFromStorage()
  if (userStore.isLoggedIn) {
    try {
      const user = await get('/users/me')
      if (user?.avatarUrl && user.avatarUrl !== userStore.avatarUrl) {
        userStore.avatarUrl = user.avatarUrl
        const info = uni.getStorageSync('userInfo')
        if (info) {
          const data = JSON.parse(info)
          data.avatarUrl = user.avatarUrl
          uni.setStorageSync('userInfo', JSON.stringify(data))
        }
      }
    } catch (e) { /* 静默失败 */ }

    if (userStore.isTutor()) {
      try {
        const cert = await get('/certifications/me')
        certStatus.value = cert?.status ?? 0
      } catch (e) { certStatus.value = 0 }
    }
  }
})

function goCertification() { uni.navigateTo({ url: '/pages/profile/certification' }) }
function goTutorProfile() { uni.navigateTo({ url: '/pages/tutor/list' }) }
function goOrders() { uni.switchTab({ url: '/pages/order/list' }) }
function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }

async function handleLogout() {
  try { await logoutApi() } catch (e) { /* ignore */ }
  userStore.logout()
  uni.reLaunch({ url: '/pages/login/index' })
}
</script>

<style lang="scss" scoped>
.page {
  background: $color-bg-page;
  min-height: 100vh;
  animation: fadeIn 0.2s $ease-default;
}

.hero {
  background: $color-primary;
  @include hero-glow;
  padding: $spacing-4xl $spacing-page $spacing-3xl;
  text-align: center;
  border-radius: 0 0 $radius-2xl $radius-2xl;
}

.hero-nickname {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: #fff;
  display: block;
  margin-top: $spacing-md;
  margin-bottom: $spacing-lg;
  position: relative;
  z-index: $z-index-normal;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xl;
  margin-bottom: $spacing-sm;
  position: relative;
  z-index: $z-index-normal;
}

.stat-item { text-align: center; }

.stat-num {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #fff;
  display: block;
}

.stat-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-light;
  color: rgba(255, 255, 255, 0.6);
  display: block;
}

.stat-divider {
  width: 1rpx;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.2);
}

.cert-badge { margin-top: $spacing-sm; position: relative; z-index: $z-index-normal; }

.menu-list {
  background: $color-bg-card;
  border-radius: $radius-xl;
  margin: $spacing-xl $spacing-page;
  overflow: hidden;
  box-shadow: $shadow-card;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-xl $spacing-2xl;
  border-bottom: 1rpx solid $color-border;

  &:last-child { border-bottom: none; }
}

.menu-text {
  font-size: $font-size-md;
  color: $color-text-primary;
  font-weight: $font-weight-regular;
}

.logout { color: $color-danger; }

.menu-arrow {
  color: $color-text-placeholder;
  font-size: $font-size-lg;
  font-weight: $font-weight-light;
}
</style>
