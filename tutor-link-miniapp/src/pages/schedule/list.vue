<template>
  <view class="page">
    <!-- 状态筛选 -->
    <scroll-view scroll-x class="tabs">
      <view
        v-for="tab in tabs" :key="tab.value"
        class="tab-item" :class="{ active: currentTab === tab.value }"
        @tap="currentTab = tab.value"
      >
        <text>{{ tab.label }}</text>
      </view>
    </scroll-view>

    <!-- 排期列表 -->
    <view v-if="filteredGroups.length">
      <view class="group-card" v-for="group in filteredGroups" :key="group.orderId">
        <view class="group-header" @tap="goOrderDetail(group.orderId)">
          <view class="user-info">
            <image class="avatar" :src="group.counterpartyAvatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
            <view class="user-text">
              <text class="nickname">{{ group.counterpartyNickname || '用户' }}</text>
              <text class="order-title">{{ group.orderTitle }}</text>
            </view>
          </view>
          <text class="arrow">›</text>
        </view>
        <view class="schedule-item" v-for="s in group.schedules" :key="s.id">
          <view class="schedule-main">
            <text class="day-text">{{ dayText(s.dayOfWeek) }}</text>
            <text class="time-text">{{ s.startTime?.substring(0,5) }}-{{ s.endTime?.substring(0,5) }}</text>
            <text class="rate-text">¥{{ s.hourlyRate / 100 }}/时</text>
            <text class="status-tag" :class="'status-' + s.status">{{ statusText(s.status) }}</text>
          </view>
          <view class="schedule-actions" v-if="s.status === 0 && s.createdByUserId != userId">
            <button class="btn-reject" @tap="handleReject(s.id)">拒绝</button>
            <button class="btn-confirm" @tap="handleConfirm(s.id)">确认</button>
          </view>
          <view class="schedule-actions" v-if="s.status === 1">
            <button class="btn-pause" @tap="handlePause(s.id)">暂停</button>
          </view>
          <view class="schedule-actions" v-if="s.status === 2">
            <button class="btn-resume" @tap="handleResume(s.id)">恢复</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && filteredGroups.length === 0" class="empty">
      <text>暂无排期</text>
    </view>

    <!-- 浮动创建按钮 -->
    <view class="fab" @tap="goCreate">
      <text class="fab-text">+</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { listMySchedules, confirmSchedule, rejectSchedule, pauseSchedule, resumeSchedule } from '../../api/schedule'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const userId = computed(() => userStore.userId)
const currentTab = ref('all')
const scheduleGroups = ref([])
const loading = ref(false)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待确认', value: 0 },
  { label: '生效中', value: 1 },
  { label: '已暂停', value: 2 }
]

onShow(() => {
  if (!userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loadData()
})

const filteredGroups = computed(() => {
  if (currentTab.value === 'all') return scheduleGroups.value
  return scheduleGroups.value.map(g => ({
    ...g,
    schedules: g.schedules.filter(s => s.status === currentTab.value)
  })).filter(g => g.schedules.length > 0)
})

async function loadData() {
  loading.value = true
  try {
    scheduleGroups.value = await listMySchedules()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function dayText(dow) {
  return ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'][dow] || ''
}

function statusText(s) {
  return { 0: '待确认', 1: '生效', 2: '暂停', 3: '已结束' }[s] || ''
}

async function handleConfirm(id) {
  try {
    await confirmSchedule(id)
    uni.showToast({ title: '已确认', icon: 'success' })
    loadData()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function handleReject(id) {
  try {
    await rejectSchedule(id)
    uni.showToast({ title: '已拒绝', icon: 'success' })
    loadData()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function handlePause(id) {
  try {
    await pauseSchedule(id)
    uni.showToast({ title: '已暂停', icon: 'success' })
    loadData()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function handleResume(id) {
  try {
    await resumeSchedule(id)
    uni.showToast({ title: '已恢复', icon: 'success' })
    loadData()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

function goOrderDetail(orderId) {
  uni.navigateTo({ url: `/pages/schedule/detail?orderId=${orderId}` })
}

function goCreate() {
  uni.navigateTo({ url: '/pages/schedule/create' })
}
</script>

<style scoped>
.page { padding-bottom: 120rpx; background: #f5f5f5; min-height: 100vh; }
.tabs { white-space: nowrap; background: #fff; padding: 20rpx; margin-bottom: 20rpx; }
.tab-item { display: inline-block; padding: 12rpx 28rpx; margin-right: 16rpx; border-radius: 32rpx; font-size: 26rpx; color: #666; background: #f0f0f0; }
.tab-item.active { background: #4A90D9; color: #fff; }

.group-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin: 0 20rpx 20rpx; }
.group-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 20rpx; border-bottom: 1rpx solid #f0f0f0; margin-bottom: 16rpx; }
.user-info { display: flex; align-items: center; }
.avatar { width: 72rpx; height: 72rpx; border-radius: 50%; margin-right: 16rpx; }
.user-text { display: flex; flex-direction: column; }
.nickname { font-size: 28rpx; font-weight: bold; color: #333; }
.order-title { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.arrow { font-size: 36rpx; color: #ccc; }

.schedule-item { padding: 16rpx 0; border-bottom: 1rpx solid #f8f8f8; }
.schedule-main { display: flex; align-items: center; gap: 16rpx; }
.day-text { font-size: 26rpx; color: #4A90D9; font-weight: bold; min-width: 64rpx; }
.time-text { font-size: 26rpx; color: #333; }
.rate-text { font-size: 24rpx; color: #FF9500; }
.status-tag { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 12rpx; margin-left: auto; }
.status-0 { background: #FFF3E0; color: #FF9800; }
.status-1 { background: #E8F5E9; color: #4CAF50; }
.status-2 { background: #F5F5F5; color: #999; }
.status-3 { background: #F5F5F5; color: #999; }

.schedule-actions { display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 12rpx; }
.btn-confirm, .btn-reject, .btn-pause, .btn-resume { font-size: 24rpx; padding: 8rpx 24rpx; border-radius: 24rpx; }
.btn-confirm { background: #4A90D9; color: #fff; }
.btn-reject { background: #fff; color: #999; border: 1rpx solid #ddd; }
.btn-pause { background: #fff; color: #FF9800; border: 1rpx solid #FF9800; }
.btn-resume { background: #4CAF50; color: #fff; }

.empty { text-align: center; padding: 120rpx; color: #999; }

.fab { position: fixed; right: 40rpx; bottom: 160rpx; width: 100rpx; height: 100rpx; border-radius: 50%; background: #4A90D9; display: flex; align-items: center; justify-content: center; box-shadow: 0 4rpx 16rpx rgba(74,144,217,0.4); }
.fab-text { color: #fff; font-size: 48rpx; font-weight: bold; }
</style>
