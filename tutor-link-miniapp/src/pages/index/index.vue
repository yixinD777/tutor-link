<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索科目、年级" @tap="goSearch" disabled />
    </view>

    <!-- 快捷入口 -->
    <view class="quick-actions">
      <view class="action-item" @tap="goTutorList" v-if="isParent">
        <text class="action-icon">🔍</text>
        <text class="action-text">找家教</text>
      </view>
      <view class="action-item" @tap="goCreateOrder" v-if="isParent">
        <text class="action-icon">📝</text>
        <text class="action-text">发布需求</text>
      </view>
      <view class="action-item" @tap="goOrderList">
        <text class="action-icon">📋</text>
        <text class="action-text">{{ isParent ? '我的订单' : '接单大厅' }}</text>
      </view>
      <view class="action-item" @tap="goChatList">
        <text class="action-icon">💬</text>
        <text class="action-text">消息</text>
      </view>
      <view class="action-item" @tap="goAdvisor" v-if="isParent">
        <text class="action-icon">🤖</text>
        <text class="action-text">AI顾问</text>
      </view>
    </view>

    <!-- 家长端：推荐家教 -->
    <view v-if="isParent" class="section">
      <view class="section-header">
        <text class="section-title">推荐家教</text>
        <text class="section-more" @tap="goTutorList">查看更多</text>
      </view>
      <view v-if="tutors.length === 0" class="empty">
        <text>暂无推荐家教</text>
      </view>
      <view v-else class="tutor-list">
        <view class="tutor-card" v-for="tutor in tutors" :key="tutor.id">
          <view class="tutor-main" @tap="goTutorDetail(tutor.userId)">
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
          <view class="tutor-actions">
            <button class="msg-btn" @tap.stop="chatWith(tutor.userId)">发消息</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 学生端：待接单需求 (Feed 流) -->
    <view v-if="isTutor" class="section">
      <view class="section-header">
        <text class="section-title">待接单需求</text>
      </view>
      <view v-if="orders.length === 0 && !isLoading" class="empty">
        <text>暂无待接单需求</text>
      </view>
      <view v-else class="order-list">
        <view class="order-card" v-for="order in orders" :key="order.id" @tap="goOrderDetail(order.id)">
          <view class="order-header">
            <text class="order-title">{{ order.title }}</text>
            <text class="order-rate">¥{{ order.hourlyRate / 100 }}/时</text>
          </view>
          <text class="order-desc">{{ order.grade }} · {{ modeText(order.teachingMode) }} · {{ order.totalHours }}课时</text>
          <text class="order-time">{{ formatTime(order.createTime) }}</text>
        </view>
      </view>
      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-tip"><text>加载中...</text></view>
      <view v-else-if="!hasMore && orders.length > 0" class="loading-tip"><text>没有更多了</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { searchTutors } from '../../api/tutor'
import { listPendingOrdersFeed } from '../../api/order'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isParent = ref(false)
const isTutor = ref(false)
const tutors = ref([])

// Feed 流状态
const orders = ref([])
const cursor = ref(null)
const hasMore = ref(true)
const isLoading = ref(false)

onMounted(() => {
  userStore.loadFromStorage()
  isParent.value = userStore.isParent()
  isTutor.value = userStore.isTutor()
  loadData()
})

onShow(() => {
  userStore.loadFromStorage()
  isParent.value = userStore.isParent()
  isTutor.value = userStore.isTutor()
})

// 触底加载更多
onReachBottom(() => {
  if (isTutor.value) loadMore()
})

function loadData() {
  if (isParent.value) loadTutors()
  if (isTutor.value) resetAndLoad()
}

async function loadTutors() {
  try {
    const res = await searchTutors({ page: 1, size: 5, sortBy: 'rating' })
    tutors.value = res.records || []
  } catch (e) { console.error(e) }
}

// 重置并加载第一页
function resetAndLoad() {
  orders.value = []
  cursor.value = null
  hasMore.value = true
  loadMore()
}

// 加载下一页
async function loadMore() {
  if (isLoading.value || !hasMore.value) return
  isLoading.value = true
  try {
    const params = { limit: 10 }
    if (cursor.value) params.cursor = cursor.value
    const res = await listPendingOrdersFeed(params)
    orders.value = [...orders.value, ...(res.list || [])]
    cursor.value = res.nextCursor
    hasMore.value = res.hasMore
  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
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

function modeText(mode) {
  return { 1: '线下', 2: '线上', 3: '均可' }[mode] || '未知'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function goSearch() { uni.navigateTo({ url: '/pages/tutor/list' }) }
function goTutorList() { uni.switchTab({ url: '/pages/tutor/list' }) }
function goCreateOrder() { uni.navigateTo({ url: '/pages/order/create' }) }
function goOrderList() { uni.switchTab({ url: '/pages/order/list' }) }
function goChatList() { uni.switchTab({ url: '/pages/chat/list' }) }
function goAdvisor() { uni.navigateTo({ url: '/pages/advisor/chat' }) }
function goTutorDetail(userId) { uni.navigateTo({ url: `/pages/tutor/detail?userId=${userId}` }) }
function goOrderDetail(id) { uni.navigateTo({ url: `/pages/order/detail?id=${id}` }) }

function chatWith(tutorUserId) {
  uni.navigateTo({ url: `/pages/chat/detail?otherUserId=${tutorUserId}` })
}
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
.loading-tip { text-align: center; padding: 30rpx; color: #999; font-size: 24rpx; }

.tutor-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.tutor-main { display: flex; }
.tutor-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; margin-right: 24rpx; }
.tutor-info { flex: 1; }
.tutor-name { font-size: 30rpx; font-weight: bold; display: block; }
.tutor-desc { font-size: 24rpx; color: #666; margin-top: 8rpx; display: block; }
.tutor-rating { margin-top: 8rpx; }
.rating-stars { color: #FF9500; font-size: 24rpx; }
.rating-count { font-size: 22rpx; color: #999; margin-left: 10rpx; }
.tutor-actions { display: flex; justify-content: flex-end; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f0f0f0; }
.msg-btn { background: #4A90D9; color: #fff; border-radius: 32rpx; font-size: 26rpx; padding: 10rpx 28rpx; line-height: 1.2; }

.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.order-header { display: flex; justify-content: space-between; align-items: center; }
.order-title { font-size: 30rpx; font-weight: bold; flex: 1; }
.order-rate { font-size: 28rpx; color: #FF9500; font-weight: bold; }
.order-desc { font-size: 24rpx; color: #666; margin-top: 8rpx; display: block; }
.order-time { font-size: 22rpx; color: #999; margin-top: 8rpx; display: block; }
</style>
