<template>
  <view class="page">
    <!-- 排期信息 -->
    <TlCard v-if="schedules.length">
      <text class="card-title">课程排期</text>
      <view v-for="s in schedules" :key="s.id" class="schedule-item">
        <view class="schedule-row">
          <text class="schedule-day">{{ weekDays[s.dayOfWeek] }}</text>
          <text class="schedule-time">{{ s.startTime?.substring(0,5) }}-{{ s.endTime?.substring(0,5) }}</text>
          <text class="schedule-rate">¥{{ s.hourlyRate / 100 }}/时</text>
          <TlStatusBadge :status="s.status" type="schedule" />
        </view>
        <text class="schedule-mode">{{ s.teachingMode === 1 ? '线下' : '线上' }} {{ s.teachingAddress || '' }}</text>
        <view v-if="s.status === 0 && s.createdByUserId != currentUserId" class="schedule-actions">
          <TlButton type="danger-outline" size="small" @tap.stop="handleReject(s.id)">拒绝</TlButton>
          <TlButton type="success" size="small" @tap.stop="handleConfirmSchedule(s.id)">确认</TlButton>
        </view>
        <view v-if="s.status === 1" class="schedule-actions">
          <TlButton type="warning" size="small" @tap.stop="handlePause(s.id)">暂停</TlButton>
        </view>
        <view v-if="s.status === 2" class="schedule-actions">
          <TlButton type="success" size="small" @tap.stop="handleResumeSchedule(s.id)">恢复</TlButton>
        </view>
      </view>
    </TlCard>

    <!-- 课时列表 -->
    <TlCard>
      <text class="card-title">课时记录</text>
      <TlEmpty v-if="sessions.length === 0" icon="📚" text="暂无课时" />
      <view v-for="s in sessions" :key="s.id" class="session-item">
        <view class="session-row">
          <text class="session-date">{{ s.lessonDate }}</text>
          <text class="session-time">{{ s.startTime?.substring(0,5) }}-{{ s.endTime?.substring(0,5) }}</text>
          <TlStatusBadge :status="s.status" type="session" />
        </view>
        <view v-if="s.status === 2 && !isParent" class="session-actions">
          <TlButton type="primary" size="small" @tap.stop="checkOut(s.id)">签退</TlButton>
        </view>
        <view v-if="s.status === 3 && isParent && !s.parentConfirm" class="session-actions">
          <TlButton type="success" size="small" @tap.stop="confirmSession(s.id)">确认课时</TlButton>
        </view>
      </view>
    </TlCard>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'
import { listSchedulesByOrder, listSessionsByOrder, confirmSchedule, rejectSchedule, pauseSchedule, resumeSchedule, checkOutSession, confirmSession as confirmSessionApi } from '../../api/schedule'
import { WEEK_DAYS } from '../../utils/constants'

const userStore = useUserStore()
const isParent = computed(() => userStore.isParent())
const currentUserId = computed(() => userStore.userId)
const orderId = ref(null)
const schedules = ref([])
const sessions = ref([])
const weekDays = WEEK_DAYS

onLoad(async (options) => {
  orderId.value = options.orderId
  await Promise.all([loadSchedules(), loadSessions()])
})

async function loadSchedules() {
  try { schedules.value = await listSchedulesByOrder(orderId.value) } catch (e) { console.error(e) }
}

async function loadSessions() {
  try { sessions.value = await listSessionsByOrder(orderId.value) } catch (e) { console.error(e) }
}

async function handleConfirmSchedule(id) {
  try { await confirmSchedule(id); uni.showToast({ title: '已确认', icon: 'success' }); loadSchedules() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}
async function handleReject(id) {
  try { await rejectSchedule(id); uni.showToast({ title: '已拒绝', icon: 'success' }); loadSchedules() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}
async function handlePause(id) {
  try { await pauseSchedule(id); uni.showToast({ title: '已暂停', icon: 'success' }); loadSchedules() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}
async function handleResumeSchedule(id) {
  try { await resumeSchedule(id); uni.showToast({ title: '已恢复', icon: 'success' }); loadSchedules() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}
async function checkOut(sessionId) {
  try { await checkOutSession(sessionId); uni.showToast({ title: '签退成功', icon: 'success' }); loadSessions() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}
async function confirmSession(sessionId) {
  try { await confirmSessionApi(sessionId); uni.showToast({ title: '已确认', icon: 'success' }); loadSessions() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}
</script>

<style lang="scss" scoped>
.page { padding: $spacing-page; }
.card-title { font-size: $font-size-md; font-weight: $font-weight-bold; margin-bottom: $spacing-md; }
.schedule-item { border-bottom: 1rpx solid $color-border; padding: $spacing-sm 0; &:last-child { border-bottom: none; } }
.schedule-row { display: flex; align-items: center; gap: $spacing-sm; }
.schedule-day { font-size: $font-size-base; font-weight: $font-weight-bold; color: $color-primary; }
.schedule-time { font-size: $font-size-base; color: $color-text-regular; }
.schedule-rate { font-size: $font-size-sm; color: $color-warning; }
.schedule-mode { font-size: $font-size-sm; color: $color-text-secondary; margin-top: $spacing-xs; }
.schedule-actions { display: flex; justify-content: flex-end; gap: $spacing-sm; margin-top: $spacing-sm; }
.session-item { border-bottom: 1rpx solid $color-border; padding: $spacing-sm 0; &:last-child { border-bottom: none; } }
.session-row { display: flex; align-items: center; gap: $spacing-sm; }
.session-date { font-size: $font-size-base; color: $color-text-regular; }
.session-time { font-size: $font-size-sm; color: $color-text-secondary; }
.session-actions { display: flex; justify-content: flex-end; margin-top: $spacing-sm; }
</style>
