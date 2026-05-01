<template>
  <view class="page">
    <view class="profile-card">
      <image class="avatar" :src="userStore.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
      <text class="nickname">{{ userStore.nickname || '未登录' }}</text>
    </view>

    <view class="menu-list">
      <view class="menu-item" @tap="goCertification" v-if="userStore.isTutor()">
        <text class="menu-text">学生认证</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goTutorProfile" v-if="userStore.isTutor()">
        <text class="menu-text">家教档案</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="switchToTutor" v-if="!userStore.isTutor()">
        <text class="menu-text">成为家教</text>
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
import { useUserStore } from '../../store/user'
import { logout as logoutApi } from '../../api/auth'
import { post } from '../../api/request'

const userStore = useUserStore()

function goCertification() { uni.navigateTo({ url: '/pages/profile/certification' }) }
function goTutorProfile() { uni.navigateTo({ url: '/pages/tutor/list' }) }
function goOrders() { uni.switchTab({ url: '/pages/order/list' }) }
function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }

async function switchToTutor() {
  try {
    await post('/tutors/me/init')
    userStore.role = userStore.role | 2
    uni.showToast({ title: '已切换为家教', icon: 'success' })
  } catch (e) { console.error(e) }
}

async function handleLogout() {
  try {
    await logoutApi()
  } catch (e) { /* ignore */ }
  userStore.logout()
  uni.reLaunch({ url: '/pages/login/index' })
}
</script>

<style scoped>
.page { padding: 20rpx; }
.profile-card { display: flex; flex-direction: column; align-items: center; background: #fff; border-radius: 16rpx; padding: 40rpx; margin-bottom: 30rpx; }
.avatar { width: 140rpx; height: 140rpx; border-radius: 50%; margin-bottom: 20rpx; }
.nickname { font-size: 34rpx; font-weight: bold; }
.menu-list { background: #fff; border-radius: 16rpx; overflow: hidden; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 30rpx; border-bottom: 1rpx solid #f0f0f0; }
.menu-item:last-child { border-bottom: none; }
.menu-text { font-size: 30rpx; }
.logout { color: #FF3B30; }
.menu-arrow { color: #ccc; font-size: 36rpx; }
</style>
