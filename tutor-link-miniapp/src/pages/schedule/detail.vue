<template>
  <view class="page">
    <!-- 排期信息 -->
    <view class="card" v-if="schedules.length">
      <view class="card-title">课程排期</view>
      <view v-for="s in schedules" :key="s.id" class="schedule-item">
        <view class="schedule-row">
          <text class="schedule-day">{{ weekDays[s.dayOfWeek] }}</text>
          <text class="schedule-time">{{ s.startTime?.substring(0,5) }}-{{ s.endTime?.substring(0,5) }}</text>
          <text class="schedule-rate">¥{{ s.hourlyRate / 100 }}/时</text>
          <text class="schedule-status" :class="'s-status-' + s.status">{{ scheduleStatusText(s.status) }}</text>
        </view>
        <text class="schedule-mode">{{ s.teachingMode === 1 ? '线下' : '线上' }} {{ s.teachingAddress || '' }}</text>
        <!-- 待确认排期：非创建者可确认/拒绝 -->
        <view v-if="s.status === 0 && s.createdByUserId != currentUserId" class="schedule-actions">
          <button class="action-btn reject" @tap.stop="handleReject(s.id)">拒绝</button>
          <button class="action-btn confirm" @tap.stop="handleConfirmSchedule(s.id)">确认</button>
        </view>
        <!-- 生效中可暂停 -->
        <view v-if="s.status === 1" class="schedule-actions">
          <button class="action-btn pause" @tap.stop="handlePause(s.id)">暂停</button>
        </view>
        <!-- 暂停中可恢复 -->
        <view v-if="s.status === 2" class="schedule-actions">
          <button class="action-btn resume" @tap.stop="handleResumeSchedule(s.id)">恢复</button>
        </view>
      </view>
    </view>

    <!-- 课时列表 -->
    <view class="card">
      <view class="card-title">课时记录</view>
      <view v-if="sessions.length === 0" class="empty"><text>暂无课时</text></view>
      <view v-for="s in sessions" :key="s.id" class="session-item">
        <view class="session-row">
          <text class="session-date">{{ s.lessonDate }}</text>
          <text class="session-time">{{ s.startTime?.substring(0,5) }}-{{ s.endTime?.substring(0,5) }}</text>
          <text class="session-status" :class="'status-' + s.status">{{ sessionStatusText(s.status) }}</text>
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
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'
import { listSchedulesByOrder, listSessionsByOrder, confirmSchedule, rejectSchedule, pauseSchedule, resumeSchedule, checkOutSession, confirmSession as confirmSessionApi } from '../../api/schedule'

const userStore = useUserStore()
const isParent = computed(() => userStore.isParent())
const currentUserId = computed(() => userStore.userId)
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
    schedules.value = await listSchedulesByOrder(orderId.value)
  } catch (e) { console.error(e) }
}

async function loadSessions() {
  try {
    sessions.value = await listSessionsByOrder(orderId.value)
  } catch (e) { console.error(e) }
}

function scheduleStatusText(s) {
  return { 0: '待确认', 1: '生效', 2: '暂停', 3: '已结束' }[s] || ''
}

function sessionStatusText(s) {
  return { 1: '待上课', 2: '进行中', 3: '已完成', 4: '缺席', 5: '已取消' }[s] || '未知'
}

async function handleConfirmSchedule(id) {
  try {
    await confirmSchedule(id)
    uni.showToast({ title: '已确认', icon: 'success' })
    loadSchedules()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function handleReject(id) {
  try {
    await rejectSchedule(id)
    uni.showToast({ title: '已拒绝', icon: 'success' })
    loadSchedules()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function handlePause(id) {
  try {
    await pauseSchedule(id)
    uni.showToast({ title: '已暂停', icon: 'success' })
    loadSchedules()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function handleResumeSchedule(id) {
  try {
    await resumeSchedule(id)
    uni.showToast({ title: '已恢复', icon: 'success' })
    loadSchedules()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function checkOut(sessionId) {
  try {
    await checkOutSession(sessionId)
    uni.showToast({ title: '签退成功', icon: 'success' })
    loadSessions()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}

async function confirmSession(sessionId) {
  try {
    await confirmSessionApi(sessionId)
    uni.showToast({ title: '已确认', icon: 'success' })
    loadSessions()
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
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
.schedule-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 12rpx; margin-left: auto; }
.s-status-0 { background: #FFF3E0; color: #FF9800; }
.s-status-1 { background: #E8F5E9; color: #4CAF50; }
.s-status-2 { background: #F5F5F5; color: #999; }
.s-status-3 { background: #F5F5F5; color: #999; }
.schedule-mode { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.schedule-actions { display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 12rpx; }
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
.action-btn { font-size: 24rpx; padding: 8rpx 24rpx; border-radius: 32rpx; line-height: 1.2; background: #4A90D9; color: #fff; }
.action-btn.confirm { background: #07C160; }
.action-btn.reject { background: #fff; color: #999; border: 1rpx solid #ddd; }
.action-btn.pause { background: #fff; color: #FF9800; border: 1rpx solid #FF9800; }
.action-btn.resume { background: #4CAF50; color: #fff; }
</style>
