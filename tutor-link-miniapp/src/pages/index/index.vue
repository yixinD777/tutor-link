<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索科目、年级" @tap="goSearch" disabled />
    </view>

    <!-- 快捷入口 -->
    <view class="quick-actions">
      <view class="action-item" @tap="goTutorList">
        <text class="action-icon">🔍</text>
        <text class="action-text">找家教</text>
      </view>
      <view class="action-item" @tap="goCreateOrder">
        <text class="action-icon">📝</text>
        <text class="action-text">发布需求</text>
      </view>
      <view class="action-item" @tap="goNearby">
        <text class="action-icon">📍</text>
        <text class="action-text">附近家教</text>
      </view>
      <view class="action-item" @tap="goCertification">
        <text class="action-icon">🎓</text>
        <text class="action-text">学生认证</text>
      </view>
    </view>

    <!-- 推荐家教 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">推荐家教</text>
        <text class="section-more" @tap="goTutorList">查看更多</text>
      </view>
      <view v-if="tutors.length === 0" class="empty">
        <text>暂无推荐家教</text>
      </view>
      <view v-else class="tutor-list">
        <view class="tutor-card" v-for="tutor in tutors" :key="tutor.id" @tap="goTutorDetail(tutor.userId)">
          <image class="tutor-avatar" :src="tutor.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
          <view class="tutor-info">
            <text class="tutor-name">{{ tutor.university || '大学生家教' }}</text>
            <text class="tutor-desc">{{ tutor.major }} · {{ formatRate(tutor.hourlyRateMin, tutor.hourlyRateMax) }}</text>
            <view class="tutor-rating">
              <text class="rating-stars">{{ formatRating(tutor.avgRating) }}</text>
              <text class="rating-count">({{ tutor.ratingCount }}评)</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { searchTutors } from '../../api/tutor'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const tutors = ref([])

onMounted(() => {
  userStore.loadFromStorage()
  loadTutors()
})

async function loadTutors() {
  try {
    const res = await searchTutors({ page: 1, size: 5, sortBy: 'rating' })
    tutors.value = res.records || []
  } catch (e) {
    console.error('Failed to load tutors', e)
  }
}

function formatRate(min, max) {
  if (!min && !max) return '价格面议'
  if (min && max) return `¥${min / 100}-${max / 100}/时`
  return `¥${(min || max) / 100}/时`
}

function formatRating(rating) {
  if (!rating || rating === 0) return '暂无评分'
  return '★'.repeat(Math.round(rating)) + '☆'.repeat(5 - Math.round(rating))
}

function goSearch() { uni.navigateTo({ url: '/pages/tutor/list' }) }
function goTutorList() { uni.switchTab({ url: '/pages/tutor/list' }) }
function goCreateOrder() { uni.navigateTo({ url: '/pages/order/create' }) }
function goNearby() { uni.navigateTo({ url: '/pages/tutor/list?nearby=true' }) }
function goCertification() { uni.navigateTo({ url: '/pages/profile/certification' }) }
function goTutorDetail(userId) { uni.navigateTo({ url: `/pages/tutor/detail?userId=${userId}` }) }
</script>

<style scoped>
.page { padding: 20rpx; }
.search-bar { margin-bottom: 30rpx; }
.search-input { background: #fff; border-radius: 40rpx; padding: 20rpx 30rpx; font-size: 28rpx; }
.quick-actions { display: flex; justify-content: space-around; margin-bottom: 40rpx; }
.action-item { display: flex; flex-direction: column; align-items: center; }
.action-icon { font-size: 48rpx; margin-bottom: 10rpx; }
.action-text { font-size: 24rpx; color: #666; }
.section { margin-bottom: 30rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 32rpx; font-weight: bold; }
.section-more { font-size: 24rpx; color: #4A90D9; }
.empty { text-align: center; padding: 60rpx; color: #999; }
.tutor-card { display: flex; background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.tutor-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; margin-right: 24rpx; }
.tutor-info { flex: 1; }
.tutor-name { font-size: 30rpx; font-weight: bold; display: block; }
.tutor-desc { font-size: 24rpx; color: #666; margin-top: 8rpx; display: block; }
.tutor-rating { margin-top: 8rpx; }
.rating-stars { color: #FF9500; font-size: 24rpx; }
.rating-count { font-size: 22rpx; color: #999; margin-left: 10rpx; }
</style>
