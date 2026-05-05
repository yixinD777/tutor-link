<template>
  <view class="page">
    <view class="form-card">
      <text class="form-title">创建课程排期</text>

      <view v-for="(item, idx) in schedules" :key="idx" class="schedule-item">
        <view class="item-header">
          <text class="item-title">排期 {{ idx + 1 }}</text>
          <text class="item-del" @tap="removeItem(idx)" v-if="schedules.length > 1">删除</text>
        </view>

        <view class="form-group">
          <text class="label">星期</text>
          <picker :range="weekDays" @change="e => item.dayOfWeek = Number(e.detail.value) + 1">
            <view class="picker-text">{{ weekDays[item.dayOfWeek - 1] }}</view>
          </picker>
        </view>

        <view class="form-row">
          <view class="form-group half">
            <text class="label">上课时间</text>
            <picker mode="time" @change="e => item.startTime = e.detail.value">
              <view class="picker-text">{{ item.startTime || '选择' }}</view>
            </picker>
          </view>
          <view class="form-group half">
            <text class="label">下课时间</text>
            <picker mode="time" @change="e => item.endTime = e.detail.value">
              <view class="picker-text">{{ item.endTime || '选择' }}</view>
            </picker>
          </view>
        </view>

        <view class="form-group">
          <text class="label">课时费(元/时)</text>
          <input class="input" v-model="item.hourlyRate" type="digit" placeholder="如: 80" />
        </view>

        <view class="form-group">
          <text class="label">生效日期</text>
          <picker mode="date" :start="today" @change="e => item.effectiveFrom = e.detail.value">
            <view class="picker-text">{{ item.effectiveFrom || '选择' }}</view>
          </picker>
        </view>
      </view>

      <button class="add-btn" @tap="addItem">+ 添加排期</button>
      <button class="submit-btn" @tap="submit">创建排期</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const orderId = ref(null)
const today = ref(new Date().toISOString().split('T')[0])
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const schedules = ref([{
  dayOfWeek: 1, startTime: '', endTime: '',
  hourlyRate: '', effectiveFrom: '', effectiveUntil: null,
  teachingAddress: '', teachingMode: 1
}])

onLoad((options) => {
  orderId.value = options.orderId
  if (options.address) schedules.value[0].teachingAddress = options.address
  if (options.mode) schedules.value[0].teachingMode = Number(options.mode)
})

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
    const res = await uni.request({
      url: '/api/v1/schedules',
      method: 'POST',
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` },
      data: { orderId: Number(orderId.value), schedules: items }
    })
    if (res.data.code === 200) {
      uni.showToast({ title: '排期创建成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1500)
    } else {
      uni.showToast({ title: res.data.message || '创建失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '请求失败', icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 20rpx; }
.form-card { background: #fff; border-radius: 16rpx; padding: 30rpx; }
.form-title { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 30rpx; }
.schedule-item { border: 2rpx solid #eee; border-radius: 12rpx; padding: 20rpx; margin-bottom: 20rpx; }
.item-header { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.item-title { font-size: 28rpx; font-weight: bold; }
.item-del { font-size: 24rpx; color: #ff4d4f; }
.form-group { margin-bottom: 16rpx; }
.form-row { display: flex; gap: 16rpx; }
.half { flex: 1; }
.label { font-size: 26rpx; color: #666; display: block; margin-bottom: 8rpx; }
.input { background: #f5f5f5; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; }
.picker-text { background: #f5f5f5; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; color: #333; }
.add-btn { background: #fff; color: #4A90D9; border: 2rpx solid #4A90D9; border-radius: 48rpx; font-size: 28rpx; padding: 20rpx; margin-bottom: 16rpx; }
.submit-btn { background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; }
</style>
