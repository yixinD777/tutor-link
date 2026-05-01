<template>
  <view class="page" v-if="tutor">
    <!-- 头部信息 -->
    <view class="profile-header">
      <image class="avatar" :src="tutor.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="basic-info">
        <text class="name">{{ tutor.university }}</text>
        <text class="meta">{{ tutor.major }} · {{ formatEdu(tutor.educationLevel) }}</text>
        <view class="rating-row">
          <text class="stars">{{ formatRating(tutor.avgRating) }}</text>
          <text class="count">{{ tutor.ratingCount }}评价 · {{ tutor.orderCount }}订单</text>
        </view>
      </view>
    </view>

    <!-- 时薪 -->
    <view class="info-card">
      <text class="label">时薪</text>
      <text class="price">{{ formatRate(tutor.hourlyRateMin, tutor.hourlyRateMax) }}</text>
    </view>

    <!-- 科目 -->
    <view class="info-card">
      <text class="label">教学科目</text>
      <view class="subject-tags">
        <text class="tag" v-for="ts in subjects" :key="ts.id">{{ ts.gradeRange || '全科' }}</text>
      </view>
    </view>

    <!-- 介绍 -->
    <view class="info-card" v-if="tutor.intro">
      <text class="label">自我介绍</text>
      <text class="content">{{ tutor.intro }}</text>
    </view>

    <!-- 教学风格 -->
    <view class="info-card" v-if="tutor.teachingStyle">
      <text class="label">教学风格</text>
      <text class="content">{{ tutor.teachingStyle }}</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar">
      <button class="chat-btn" @tap="goChat">在线沟通</button>
      <button class="order-btn" @tap="goCreateOrder">预约试课</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTutorDetail, getTutorSubjects } from '../../api/tutor'

const tutor = ref(null)
const subjects = ref([])
const userId = ref('')

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  userId.value = currentPage.options.userId
  loadDetail()
})

async function loadDetail() {
  try {
    const [profile, subs] = await Promise.all([
      getTutorDetail(userId.value),
      getTutorSubjects(userId.value)
    ])
    tutor.value = profile
    subjects.value = subs || []
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function formatRate(min, max) {
  if (!min && !max) return '价格面议'
  if (min && max) return `¥${min / 100}-${max / 100}/时`
  return `¥${(min || max) / 100}/时`
}

function formatEdu(level) {
  const map = { 1: '本科', 2: '硕士', 3: '博士' }
  return map[level] || '本科'
}

function formatRating(rating) {
  if (!rating || rating === 0) return '暂无评分'
  return '★'.repeat(Math.round(rating))
}

function goChat() { uni.navigateTo({ url: `/pages/chat/list?tutorId=${userId.value}` }) }
function goCreateOrder() { uni.navigateTo({ url: `/pages/order/create?tutorId=${userId.value}` }) }
</script>

<style scoped>
.page { padding: 20rpx; padding-bottom: 140rpx; }
.profile-header { display: flex; background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 20rpx; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 50%; margin-right: 24rpx; }
.basic-info { flex: 1; }
.name { font-size: 36rpx; font-weight: bold; display: block; }
.meta { font-size: 26rpx; color: #666; margin-top: 8rpx; display: block; }
.rating-row { margin-top: 8rpx; }
.stars { color: #FF9500; font-size: 26rpx; }
.count { font-size: 22rpx; color: #999; margin-left: 12rpx; }
.info-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.label { font-size: 28rpx; font-weight: bold; display: block; margin-bottom: 12rpx; }
.price { font-size: 36rpx; color: #FF6B35; font-weight: bold; }
.subject-tags { display: flex; flex-wrap: wrap; gap: 12rpx; }
.tag { background: #E3F2FD; color: #4A90D9; padding: 8rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; }
.content { font-size: 28rpx; color: #666; line-height: 1.6; }
.action-bar { position: fixed; bottom: 0; left: 0; right: 0; display: flex; padding: 20rpx; background: #fff; gap: 20rpx; }
.chat-btn { flex: 1; background: #fff; color: #4A90D9; border: 2rpx solid #4A90D9; border-radius: 48rpx; font-size: 30rpx; padding: 20rpx; }
.order-btn { flex: 1; background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 30rpx; padding: 20rpx; }
</style>
