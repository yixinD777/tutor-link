<template>
  <view class="page">
    <!-- 顶部周切换栏 -->
    <view class="header">
      <view class="week-nav">
        <view class="nav-btn" @tap="prevWeek">
          <text class="nav-arrow">‹</text>
        </view>
        <text class="week-title">{{ weekTitle }}</text>
        <view class="nav-btn" @tap="nextWeek">
          <text class="nav-arrow">›</text>
        </view>
      </view>
      <!-- 日期行 -->
      <view class="date-row">
        <view class="time-col-placeholder"></view>
        <view
          v-for="(day, i) in weekDates"
          :key="i"
          class="date-cell"
          :class="{ today: isToday(day) }"
        >
          <text class="weekday-label">{{ WEEK_DAYS[dateGetDow(day)] }}</text>
          <view class="date-badge" :class="{ 'today-badge': isToday(day) }">
            <text class="date-num">{{ day.getDate() }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 课表主体（滚动区） -->
    <scroll-view scroll-y class="body">
      <view class="grid">
        <!-- 时间刻度列 -->
        <view class="time-col">
          <view
            v-for="slot in timeSlots"
            :key="slot"
            class="time-slot"
          >
            <text class="time-label">{{ slot }}</text>
          </view>
        </view>

        <!-- 7列课程列 (周日..周六，对应 getDay 0-6) -->
        <view
          v-for="(day, colIdx) in weekDates"
          :key="colIdx"
          class="day-col"
        >
          <!-- 时间分隔线背景 -->
          <view
            v-for="slot in timeSlots"
            :key="slot"
            class="slot-bg"
          ></view>

          <!-- 排期色块 -->
          <view
            v-for="block in getBlocks(day)"
            :key="block.id"
            class="course-block"
            :style="block.style"
            :class="'color-' + (block.colorIndex % 6)"
            @tap="goDetail(block.orderId)"
          >
            <text class="block-subject">{{ block.subject }}</text>
            <text class="block-time">{{ block.startTime }}-{{ block.endTime }}</text>
            <text v-if="block.nickname" class="block-name">{{ block.nickname }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 空状态 -->
    <TlEmpty
      v-if="!loading && allBlocks.length === 0"
      class="week-empty"
      icon="📅"
      text="本周暂无课程"
      action-text="去排期设置"
      @action="goScheduleList"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { listMySchedules } from '../../api/schedule'
import { useUserStore } from '../../store/user'
import { timeToHour, dateGetDow, isToday, getMonday } from '../../utils/formatters'
import { WEEK_DAYS, SLOT_HEIGHT, HOUR_START, HOUR_END } from '../../utils/constants'

const userStore = useUserStore()

// ---------- 周导航 ----------
const weekOffset = ref(0)   // 0=本周, -1=上周, 1=下周

const weekDates = computed(() => {
  const monday = getMonday(weekOffset.value)
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    return d
  })
})

const weekTitle = computed(() => {
  const [start, end] = [weekDates.value[0], weekDates.value[6]]
  const fmt = d => `${d.getMonth() + 1}月${d.getDate()}日`
  return `${fmt(start)} - ${fmt(end)}`
})

function prevWeek() { weekOffset.value-- }
function nextWeek() { weekOffset.value++ }

// ---------- 时间刻度 ----------
const timeSlots = computed(() => {
  const slots = []
  for (let h = HOUR_START; h <= HOUR_END; h++) {
    slots.push(`${String(h).padStart(2, '0')}:00`)
  }
  return slots
})

// ---------- 排期数据 ----------
const loading = ref(false)
const scheduleGroups = ref([])

onShow(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    scheduleGroups.value = await listMySchedules() || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 所有生效中的排期 (status=1)
const activeSchedules = computed(() => {
  const result = []
  scheduleGroups.value.forEach((group, gi) => {
    group.schedules
      .filter(s => s.status === 1)
      .forEach(s => {
        result.push({
          ...s,
          orderTitle: group.orderTitle,
          counterpartyNickname: group.counterpartyNickname,
          counterpartyAvatarUrl: group.counterpartyAvatarUrl,
          colorIndex: gi
        })
      })
  })
  return result
})

const allBlocks = computed(() => activeSchedules.value)

// 当前列（weekDates[i]）对应的 getDay() 之 dow 映射
function getBlocks(date) {
  const dow = dateGetDow(date)
  return activeSchedules.value
    .filter(s => s.dayOfWeek === dow)
    .map(s => {
      const startH = timeToHour(s.startTime, HOUR_START)
      const endH = timeToHour(s.endTime, HOUR_START)
      const topRpx = (startH - HOUR_START) * SLOT_HEIGHT
      const heightRpx = Math.max((endH - startH) * SLOT_HEIGHT, 40)
      return {
        id: s.id,
        orderId: s.orderId,
        subject: s.orderTitle || '课程',
        startTime: s.startTime?.substring(0, 5),
        endTime: s.endTime?.substring(0, 5),
        nickname: s.counterpartyNickname,
        colorIndex: s.colorIndex,
        style: `top:${topRpx}rpx;height:${heightRpx}rpx;`
      }
    })
}

function goDetail(orderId) {
  if (orderId) {
    uni.navigateTo({ url: `/pages/schedule/detail?orderId=${orderId}` })
  }
}

function goScheduleList() {
  uni.switchTab({ url: '/pages/schedule/list' })
}
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $color-bg-page;
}

/* ---- 顶部 header ---- */
.header {
  background: $color-bg-card;
  padding-bottom: 0;
  box-shadow: $shadow-bar;
  z-index: $z-index-sticky;
}

.week-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-lg $spacing-xl $spacing-sm;
}

.nav-btn {
  width: 56rpx;
  height: 56rpx;
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

.week-title {
  font-size: $font-size-sm;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
}

/* 日期行 */
.date-row {
  display: flex;
  align-items: flex-end;
  padding: 0 0 $spacing-sm;
}

.time-col-placeholder {
  width: 80rpx;
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
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-round;
}

.today-badge {
  background: $color-primary;
}

.date-num {
  font-size: $font-size-md;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
}

.today .weekday-label {
  color: $color-primary;
}

.today-badge .date-num {
  color: $color-bg-card;
}

/* ---- 主体网格 ---- */
.body {
  flex: 1;
  overflow: hidden;
}

.grid {
  display: flex;
  flex-direction: row;
  /* 总高度 = (HOUR_END - HOUR_START + 1) * SLOT_HEIGHT rpx */
  min-height: 1200rpx;
}

/* 时间刻度列 */
.time-col {
  width: 80rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.time-slot {
  height: 80rpx;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding-right: $spacing-sm;
  padding-top: 4rpx;
  border-top: 1rpx solid $color-border;
}

.time-label {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}

/* 每天一列 */
.day-col {
  flex: 1;
  position: relative;
  border-left: 1rpx solid $color-border;
}

.slot-bg {
  height: 80rpx;
  border-top: 1rpx solid $color-border;
}

/* 课程色块 */
.course-block {
  position: absolute;
  left: 4rpx;
  right: 4rpx;
  border-radius: $radius-sm;
  padding: 6rpx 8rpx;
  overflow: hidden;
  z-index: $z-index-normal;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
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
  line-height: $line-height-tight;
}

.block-name {
  font-size: 17rpx;
  color: rgba(255, 255, 255, 0.75);
  margin-top: 2rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 6种配色循环 */
.color-0 { background: #F48FB1; }  /* 粉红 */
.color-1 { background: $color-primary; }  /* 蓝   */
.color-2 { background: #26C6DA; }  /* 青绿 */
.color-3 { background: $color-warning; }  /* 橙   */
.color-4 { background: $color-success; }  /* 绿   */
.color-5 { background: #AB47BC; }  /* 紫   */

/* 周视图空状态 */
.week-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
