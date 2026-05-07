<template>
  <view class="page">

    <!-- 顶部工具栏：左边筛选 tabs（列表模式），或日期导航（周视图模式），右边视图切换 -->
    <view class="toolbar">

      <!-- 列表模式：状态筛选 -->
      <template v-if="viewMode === 'list'">
        <TlScrollTabs
          class="toolbar-tabs"
          :tabs="tabs"
          v-model="currentTab"
        />
      </template>

      <!-- 周视图模式：日期导航 -->
      <template v-else>
        <view class="week-nav">
          <view class="nav-btn" @tap="prevWeek"><text class="nav-arrow">‹</text></view>
          <text class="week-range">{{ weekRange }}</text>
          <view class="nav-btn" @tap="nextWeek"><text class="nav-arrow">›</text></view>
        </view>
      </template>

      <!-- 视图切换按钮 -->
      <view class="view-toggle" @tap="toggleView">
        <text class="toggle-icon">{{ viewMode === 'list' ? '⊞' : '≡' }}</text>
      </view>
    </view>

    <!-- ============ 列表视图 ============ -->
    <view v-if="viewMode === 'list'" class="list-body">
      <TlLoading v-if="loading" text="加载中..." />

      <template v-else-if="filteredGroups.length">
        <TlCard
          v-for="group in filteredGroups"
          :key="group.orderId"
          class="group-card"
        >
          <template #header>
            <view class="group-header" @tap="goOrderDetail(group.orderId)">
              <view class="user-info">
                <TlAvatar :src="group.counterpartyAvatarUrl" :name="group.counterpartyNickname || '用户'" />
                <view class="user-text">
                  <text class="nickname">{{ group.counterpartyNickname || '用户' }}</text>
                  <text class="order-title">{{ group.orderTitle }}</text>
                </view>
              </view>
              <text class="arrow">›</text>
            </view>
          </template>

          <view class="schedule-item" v-for="s in group.schedules" :key="s.id">
            <view class="schedule-main">
              <text class="day-text">{{ dayText(s.dayOfWeek) }}</text>
              <text class="time-text">{{ s.startTime?.substring(0,5) }}-{{ s.endTime?.substring(0,5) }}</text>
              <text class="rate-text">{{ formatPrice(s.hourlyRate) }}/时</text>
              <TlStatusBadge :status="s.status" type="schedule" class="schedule-badge" />
            </view>
            <view class="schedule-actions" v-if="s.status === 0 && s.createdByUserId != userId">
              <TlButton type="danger-outline" size="small" :block="false" @tap="handleReject(s.id)">拒绝</TlButton>
              <TlButton type="primary" size="small" :block="false" @tap="handleConfirm(s.id)">确认</TlButton>
            </view>
            <view class="schedule-actions" v-if="s.status === 1">
              <TlButton type="warning" size="small" :block="false" @tap="handlePause(s.id)">暂停</TlButton>
            </view>
            <view class="schedule-actions" v-if="s.status === 2">
              <TlButton type="success" size="small" :block="false" @tap="handleResume(s.id)">恢复</TlButton>
            </view>
          </view>
        </TlCard>
      </template>

      <TlEmpty v-else icon="📅" text="暂无排期" />
    </view>

    <!-- ============ 周视图 ============ -->
    <view v-else class="week-body">
      <!-- 日期列头 -->
      <view class="date-row">
        <view class="time-col-placeholder"></view>
        <view
          v-for="(day, i) in weekDates" :key="i"
          class="date-cell" :class="{ today: isToday(day) }"
        >
          <text class="weekday-label">{{ WEEK_DAYS[dateGetDow(day)] }}</text>
          <view class="date-badge" :class="{ 'today-badge': isToday(day) }">
            <text class="date-num">{{ day.getDate() }}</text>
          </view>
        </view>
      </view>

      <!-- 网格滚动区 -->
      <scroll-view scroll-y class="grid-scroll">
        <view class="grid">
          <!-- 时间刻度 -->
          <view class="time-col">
            <view v-for="slot in timeSlots" :key="slot" class="time-slot">
              <text class="time-label">{{ slot }}</text>
            </view>
          </view>
          <!-- 7 天列 -->
          <view v-for="(day, ci) in weekDates" :key="ci" class="day-col">
            <view v-for="slot in timeSlots" :key="slot" class="slot-bg"></view>
            <view
              v-for="block in getBlocks(day)" :key="block.id"
              class="course-block" :style="block.style" :class="'color-' + (block.colorIndex % 6)"
              @tap="goOrderDetail(block.orderId)"
            >
              <text class="block-subject">{{ block.subject }}</text>
              <text class="block-time">{{ block.startTime }}-{{ block.endTime }}</text>
              <text v-if="block.nickname" class="block-name">{{ block.nickname }}</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <TlEmpty
        v-if="!loading && activeSchedules.length === 0"
        class="week-empty"
        icon="📅"
        text="本周暂无生效课程"
      />
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
import { scheduleStatusText, formatPrice, timeToHour, dateGetDow, isToday, getMonday } from '../../utils/formatters'
import { WEEK_DAYS, SLOT_HEIGHT, HOUR_START, HOUR_END } from '../../utils/constants'

const userStore = useUserStore()
const userId = computed(() => userStore.userId)

// ---- 视图切换 ----
const viewMode = ref('list')   // 'list' | 'week'
function toggleView() {
  viewMode.value = viewMode.value === 'list' ? 'week' : 'list'
}

// ---- 列表视图：筛选 ----
const currentTab = ref('all')
const tabs = [
  { label: '全部', value: 'all' },
  { label: '待确认', value: 0 },
  { label: '生效中', value: 1 },
  { label: '已暂停', value: 2 }
]

// ---- 周视图：日期导航 ----
const weekOffset = ref(0)

const weekDates = computed(() => {
  const monday = getMonday(weekOffset.value)
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    return d
  })
})

const weekRange = computed(() => {
  const [start, end] = [weekDates.value[0], weekDates.value[6]]
  const fmt = d => `${d.getMonth() + 1}月${d.getDate()}日`
  return `${fmt(start)} - ${fmt(end)}`
})

function prevWeek() { weekOffset.value-- }
function nextWeek() { weekOffset.value++ }

// ---- 时间网格 ----
const timeSlots = computed(() => {
  const s = []
  for (let h = HOUR_START; h <= HOUR_END; h++) s.push(`${String(h).padStart(2, '0')}:00`)
  return s
})

// ---- 数据 ----
const loading = ref(false)
const scheduleGroups = ref([])

onShow(() => {
  if (!userStore.isLoggedIn) { uni.reLaunch({ url: '/pages/login/index' }); return }
  loadData()
})

async function loadData() {
  loading.value = true
  try { scheduleGroups.value = await listMySchedules() || [] }
  catch (e) { console.error(e) }
  finally { loading.value = false }
}

// 列表视图过滤
const filteredGroups = computed(() => {
  if (currentTab.value === 'all') return scheduleGroups.value
  return scheduleGroups.value.map(g => ({
    ...g, schedules: g.schedules.filter(s => s.status === currentTab.value)
  })).filter(g => g.schedules.length > 0)
})

// 周视图：仅生效排期
const activeSchedules = computed(() => {
  const result = []
  scheduleGroups.value.forEach((group, gi) => {
    group.schedules.filter(s => s.status === 1).forEach(s => {
      result.push({ ...s, orderTitle: group.orderTitle, counterpartyNickname: group.counterpartyNickname, colorIndex: gi })
    })
  })
  return result
})

function getBlocks(date) {
  const dow = dateGetDow(date)
  return activeSchedules.value.filter(s => s.dayOfWeek === dow).map(s => {
    const startH = timeToHour(s.startTime, HOUR_START)
    const endH = timeToHour(s.endTime, HOUR_START)
    const top = (startH - HOUR_START) * SLOT_HEIGHT
    const height = Math.max((endH - startH) * SLOT_HEIGHT, 40)
    return {
      id: s.id,
      orderId: s.orderId,
      subject: s.orderTitle || '课程',
      startTime: s.startTime?.substring(0, 5),
      endTime: s.endTime?.substring(0, 5),
      nickname: s.counterpartyNickname,
      colorIndex: s.colorIndex,
      style: `top:${top}rpx;height:${height}rpx;`
    }
  })
}

// ---- 操作 ----
function dayText(dow) { return WEEK_DAYS[dow] || '' }

async function handleConfirm(id) {
  try { await confirmSchedule(id); uni.showToast({ title:'已确认', icon:'success' }); loadData() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon:'none' }) }
}
async function handleReject(id) {
  try { await rejectSchedule(id); uni.showToast({ title:'已拒绝', icon:'success' }); loadData() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon:'none' }) }
}
async function handlePause(id) {
  try { await pauseSchedule(id); uni.showToast({ title:'已暂停', icon:'success' }); loadData() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon:'none' }) }
}
async function handleResume(id) {
  try { await resumeSchedule(id); uni.showToast({ title:'已恢复', icon:'success' }); loadData() }
  catch (e) { uni.showToast({ title: e.message || '操作失败', icon:'none' }) }
}

function goOrderDetail(orderId) { uni.navigateTo({ url: `/pages/schedule/detail?orderId=${orderId}` }) }
function goCreate() { uni.navigateTo({ url: '/pages/schedule/create' }) }
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $color-bg-page;
}

/* ---- 工具栏 ---- */
.toolbar {
  display: flex;
  align-items: center;
  background: $color-bg-card;
  border-bottom: 1rpx solid $color-border;
  flex-shrink: 0;
}

/* 列表模式：tabs 占满剩余宽度 */
.toolbar-tabs {
  flex: 1;
  margin-bottom: 0;
}

/* 周视图模式：日期导航居中 */
.week-nav {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-lg;
  padding: $spacing-lg 0;
}

.nav-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-round;
  background: $color-bg-hover;
}

.nav-arrow {
  font-size: $font-size-lg;
  color: $color-primary;
  line-height: 1;
}

.week-range {
  font-size: $font-size-md;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
  min-width: 280rpx;
  text-align: center;
}

/* 视图切换图标 */
.view-toggle {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.toggle-icon {
  font-size: $font-size-lg;
  color: $color-primary;
}

/* ---- 列表视图 ---- */
.list-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-page $spacing-page 120rpx;
}

.group-card {
  margin-bottom: $spacing-md;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-text {
  display: flex;
  flex-direction: column;
  margin-left: $spacing-md;
}

.nickname {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.order-title {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-top: 4rpx;
}

.arrow {
  font-size: $font-size-lg;
  color: $color-text-placeholder;
}

.schedule-item {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $color-border;
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-main {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.day-text {
  font-size: $font-size-sm;
  color: $color-primary;
  font-weight: $font-weight-bold;
  min-width: 64rpx;
}

.time-text {
  font-size: $font-size-sm;
  color: $color-text-primary;
}

.rate-text {
  font-size: $font-size-sm;
  color: $color-warning;
}

.schedule-badge {
  margin-left: auto;
}

.schedule-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
  margin-top: $spacing-sm;
}

/* ---- 周视图 ---- */
.week-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.date-row {
  display: flex;
  align-items: flex-end;
  background: $color-bg-card;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $color-border;
  flex-shrink: 0;
}

.time-col-placeholder {
  width: 72rpx;
  flex-shrink: 0;
}

.date-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.weekday-label {
  font-size: $font-size-xs;
  color: $color-text-secondary;
}

.date-badge {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-round;
}

.today-badge {
  background: $color-primary;
}

.date-num {
  font-size: $font-size-sm;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
}

.today .weekday-label {
  color: $color-primary;
}

.today-badge .date-num {
  color: $color-bg-card;
}

.grid-scroll {
  flex: 1;
}

.grid {
  display: flex;
}

.time-col {
  width: 72rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.time-slot {
  height: 80rpx;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding-right: 10rpx;
  padding-top: 4rpx;
  border-top: 1rpx solid $color-border;
}

.time-label {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}

.day-col {
  flex: 1;
  position: relative;
  border-left: 1rpx solid $color-border;
}

.slot-bg {
  height: 80rpx;
  border-top: 1rpx solid $color-border;
}

.course-block {
  position: absolute;
  left: 3rpx;
  right: 3rpx;
  border-radius: $radius-sm;
  padding: 5rpx 7rpx;
  overflow: hidden;
  z-index: $z-index-normal;
  display: flex;
  flex-direction: column;
}

.block-subject {
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  color: $color-bg-card;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  line-height: $line-height-tight;
}

.block-time {
  font-size: 17rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 2rpx;
}

.block-name {
  font-size: 17rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 2rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 6种配色循环 */
.color-0 { background: #F48FB1; }
.color-1 { background: $color-primary; }
.color-2 { background: #26C6DA; }
.color-3 { background: $color-warning; }
.color-4 { background: $color-success; }
.color-5 { background: #AB47BC; }

/* 周视图空状态 */
.week-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* ---- FAB ---- */
.fab {
  position: fixed;
  right: 40rpx;
  bottom: 160rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: $radius-round;
  background: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-float;
}

.fab-text {
  color: $color-bg-card;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
}
</style>
