<template>
  <view class="page">
    <!-- 排期信息 -->
    <view class="card" v-if="schedules.length">
      <view class="card-title">课程排期</view>
      <view v-for="s in schedules" :key="s.id" class="schedule-item">
        <view class="schedule-row">
          <text class="schedule-day">{{ weekDays[s.dayOfWeek] }}</text>
          <text class="schedule-time">{{ s.startTime }}-{{ s.endTime }}</text>
          <text class="schedule-rate">¥{{ s.hourlyRate / 100 }}/时</text>
        </view>
        <text class="schedule-mode">{{ s.teachingMode === 1 ? '线下' : '线上' }} {{ s.teachingAddress || '' }}</text>
      </view>
    </view>

    <!-- 课时列表 -->
    <view class="card">
      <view class="card-title">课时记录</view>
      <view v-if="sessions.length === 0" class="empty"><text>暂无课时</text></view>
      <view v-for="s in sessions" :key="s.id" class="session-item" @tap="goSessionDetail(s)">
        <view class="session-row">
          <text class="session-date">{{ s.lessonDate }}</text>
          <text class="session-time">{{ s.startTime }}-{{ s.endTime }}</text>
          <text class="session-status" :class="'status-' + s.status">{{ statusText(s.status) }}</text>
        </view>
        <view v-if="s.status === 2 && !isParent" class="session-actions">
          <button class="action-btn" @tap.stop="checkOut(s.id)">签退</button>
        </view>
        <view v-if="s.status === 3 && isParent && !s.parentConfirm" class="session-actions">
          <button class="action-btn confirm" @tap.stop="confirmSession(s.id)">确认课时</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isParent = userStore.isParent()
const orderId = ref(null)
const schedules = ref([])
const sessions = ref([])
const weekDays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

onLoad(async (options) => {
  orderId.value = options.orderId
  await Promise.all([loadSchedules(), loadSessions()])
})

async function loadSchedules() {
  try {
    const res = await uni.request({
      url: `/api/v1/schedules/order/${orderId.value}`,
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` }
    })
    if (res.data.code === 200) schedules.value = res.data.data || []
  } catch (e) { console.error(e) }
}

async function loadSessions() {
  try {
    const res = await uni.request({
      url: `/api/v1/schedules/order/${orderId.value}/sessions`,
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` }
    })
    if (res.data.code === 200) sessions.value = res.data.data || []
  } catch (e) { console.error(e) }
}

function statusText(s) {
  return { 1: '待上课', 2: '进行中', 3: '已完成', 4: '缺席', 5: '已取消' }[s] || '未知'
}

async function checkOut(sessionId) {
  try {
    const res = await uni.request({
      url: `/api/v1/schedules/sessions/${sessionId}/check-out`,
      method: 'PUT',
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` }
    })
    if (res.data.code === 200) {
      uni.showToast({ title: '签退成功', icon: 'success' })
      loadSessions()
    }
  } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

async function confirmSession(sessionId) {
  try {
    const res = await uni.request({
      url: `/api/v1/schedules/sessions/${sessionId}/confirm`,
      method: 'PUT',
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` }
    })
    if (res.data.code === 200) {
      uni.showToast({ title: '已确认', icon: 'success' })
      loadSessions()
    }
  } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

function goSessionDetail(s) {
  // 可扩展为跳转到课时详情页
}
</script>

<style scoped>
.page { padding: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.schedule-item { border-bottom: 1rpx solid #f5f5f5; padding: 16rpx 0; }
.schedule-row { display: flex; align-items: center; gap: 16rpx; }
.schedule-day { font-size: 28rpx; font-weight: bold; color: #4A90D9; }
.schedule-time { font-size: 28rpx; color: #333; }
.schedule-rate { font-size: 26rpx; color: #FF9500; }
.schedule-mode { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.empty { text-align: center; padding: 40rpx; color: #999; }
.session-item { border-bottom: 1rpx solid #f5f5f5; padding: 16rpx 0; }
.session-row { display: flex; align-items: center; gap: 16rpx; }
.session-date { font-size: 28rpx; color: #333; }
.session-time { font-size: 26rpx; color: #666; }
.session-status { font-size: 24rpx; padding: 4rpx 12rpx; border-radius: 8rpx; }
.status-1 { background: #FFF8E1; color: #FF9500; }
.status-2 { background: #E3F2FD; color: #4A90D9; }
.status-3 { background: #E8F5E9; color: #07C160; }
.status-4 { background: #FFEBEE; color: #ff4d4f; }
.status-5 { background: #F5F5F5; color: #999; }
.session-actions { display: flex; justify-content: flex-end; margin-top: 12rpx; }
.action-btn { background: #4A90D9; color: #fff; border-radius: 32rpx; font-size: 24rpx; padding: 8rpx 24rpx; line-height: 1.2; }
.action-btn.confirm { background: #07C160; }
</style>
