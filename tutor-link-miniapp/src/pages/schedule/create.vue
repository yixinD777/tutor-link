<template>
  <view class="page">
    <TlCard>
      <text class="form-title">创建课程排期</text>

      <!-- 订单选择（无 orderId 时显示） -->
      <TlFormInput v-if="!orderId" label="选择订单">
        <template #default>
          <picker :range="orderLabels" @change="onOrderChange">
            <view class="picker-text">{{ selectedOrderLabel || '请选择订单' }}</view>
          </picker>
        </template>
      </TlFormInput>

      <view v-for="(item, idx) in schedules" :key="idx" class="schedule-item">
        <view class="item-header">
          <text class="item-title">排期 {{ idx + 1 }}</text>
          <text class="item-del" @tap="removeItem(idx)" v-if="schedules.length > 1">删除</text>
        </view>

        <TlFormInput label="星期">
          <template #default>
            <picker :range="weekDays" @change="e => item.dayOfWeek = Number(e.detail.value) + 1">
              <view class="picker-text">{{ weekDays[item.dayOfWeek - 1] }}</view>
            </picker>
          </template>
        </TlFormInput>

        <view class="form-row">
          <view class="half">
            <text class="label">上课时间</text>
            <picker mode="time" @change="e => item.startTime = e.detail.value">
              <view class="picker-text">{{ item.startTime || '选择' }}</view>
            </picker>
          </view>
          <view class="half">
            <text class="label">下课时间</text>
            <picker mode="time" @change="e => item.endTime = e.detail.value">
              <view class="picker-text">{{ item.endTime || '选择' }}</view>
            </picker>
          </view>
        </view>

        <TlFormInput label="课时费(元/时)" v-model="item.hourlyRate" type="digit" placeholder="如: 80" />

        <TlFormInput label="生效日期">
          <template #default>
            <picker mode="date" :start="today" @change="e => item.effectiveFrom = e.detail.value">
              <view class="picker-text">{{ item.effectiveFrom || '选择' }}</view>
            </picker>
          </template>
        </TlFormInput>
      </view>

      <TlButton type="outline" @tap="addItem" style="margin-bottom: 16rpx;">+ 添加排期</TlButton>
      <TlButton @tap="submit">提交排期</TlButton>
    </TlCard>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { post, get } from '../../api/request'
import { WEEK_DAYS_PICKER } from '../../utils/constants'

const orderId = ref(null)
const today = ref(new Date().toISOString().split('T')[0])
const weekDays = WEEK_DAYS_PICKER

// 订单选择器
const orders = ref([])
const selectedOrderIdx = ref(-1)
const orderLabels = computed(() => orders.value.map(o => `${o.title} - ¥${o.hourlyRate / 100}/时`))
const selectedOrderLabel = computed(() => selectedOrderIdx.value >= 0 ? orderLabels.value[selectedOrderIdx.value] : '')

const schedules = ref([{
  dayOfWeek: 1, startTime: '', endTime: '',
  hourlyRate: '', effectiveFrom: '', effectiveUntil: null,
  teachingAddress: '', teachingMode: 1
}])

onLoad((options) => {
  if (options.orderId) {
    orderId.value = options.orderId
  } else {
    loadOrders()
  }
  if (options.address) schedules.value[0].teachingAddress = options.address
  if (options.mode) schedules.value[0].teachingMode = Number(options.mode)
})

async function loadOrders() {
  try {
    const data = await get('/orders', { page: 1, size: 50, role: 0 })
    orders.value = (data.records || []).filter(o => o.status === 3 || o.status === 4)
  } catch (e) { console.error(e) }
}

function onOrderChange(e) {
  selectedOrderIdx.value = Number(e.detail.value)
  if (orders.value[selectedOrderIdx.value]) {
    orderId.value = orders.value[selectedOrderIdx.value].id
    if (orders.value[selectedOrderIdx.value].hourlyRate) {
      schedules.value.forEach(s => {
        if (!s.hourlyRate) s.hourlyRate = (orders.value[selectedOrderIdx.value].hourlyRate / 100).toString()
      })
    }
  }
}

function addItem() {
  schedules.value.push({
    dayOfWeek: 1, startTime: '', endTime: '',
    hourlyRate: '', effectiveFrom: '', effectiveUntil: null,
    teachingAddress: schedules.value[0].teachingAddress,
    teachingMode: schedules.value[0].teachingMode
  })
}

function removeItem(idx) { schedules.value.splice(idx, 1) }

async function submit() {
  if (!orderId.value) {
    uni.showToast({ title: '请选择订单', icon: 'none' }); return
  }

  const items = schedules.value.map(s => ({
    ...s,
    hourlyRate: Math.round(parseFloat(s.hourlyRate) * 100)
  }))
  for (const item of items) {
    if (!item.startTime || !item.endTime || !item.hourlyRate || !item.effectiveFrom) {
      uni.showToast({ title: '请填写完整信息', icon: 'none' }); return
    }
  }

  try {
    await post('/schedules', { orderId: orderId.value, schedules: items })
    uni.showToast({ title: '已提交，等待对方确认', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '创建失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page {
  padding: $spacing-page;
}

.form-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  display: block;
  margin-bottom: $spacing-xl;
}

.schedule-item {
  border: 2rpx solid $color-border;
  border-radius: $radius-md;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: $spacing-md;
}

.item-title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.item-del {
  font-size: $font-size-sm;
  color: $color-danger;
}

.form-row {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.half {
  flex: 1;
}

.label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  display: block;
  margin-bottom: $spacing-xs;
}

.picker-text {
  background: $color-bg-input;
  border-radius: $radius-md;
  padding: $spacing-md;
  font-size: $font-size-base;
  color: $color-text-regular;
}
</style>
