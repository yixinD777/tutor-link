<template>
  <view class="page">
    <view class="form-card">
      <text class="form-title">创建试课</text>

      <view class="form-group">
        <text class="label">试课日期时间</text>
        <picker mode="date" :start="today" @change="onDateChange">
          <view class="picker-text">{{ date || '请选择日期' }}</view>
        </picker>
        <picker mode="time" @change="onTimeChange">
          <view class="picker-text">{{ time || '请选择时间' }}</view>
        </picker>
      </view>

      <view class="form-group">
        <text class="label">试课时长(分钟)</text>
        <input class="input" v-model="form.trialDuration" type="number" placeholder="如: 60" />
      </view>

      <view class="form-group">
        <text class="label">试课价格(元)</text>
        <input class="input" v-model="form.trialPrice" type="digit" placeholder="如: 50" />
      </view>

      <view class="form-group">
        <text class="label">授课方式</text>
        <picker :range="['线下', '线上']" @change="onModeChange">
          <view class="picker-text">{{ modeText }}</view>
        </picker>
      </view>

      <view class="form-group" v-if="form.trialMode === 1">
        <text class="label">试课地址</text>
        <input class="input" v-model="form.trialAddress" placeholder="请输入地址" />
      </view>

      <button class="submit-btn" @tap="submit">发送试课邀请</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { sendMessage } from '../../api/chat'

const orderId = ref(null)
const tutorUserId = ref(null)
const date = ref('')
const time = ref('')
const today = ref(new Date().toISOString().split('T')[0])
const modeText = ref('线下')

const form = ref({
  trialDuration: 60,
  trialPrice: '',
  trialAddress: '',
  trialMode: 1
})

onLoad((options) => {
  orderId.value = options.orderId
  tutorUserId.value = options.tutorUserId
})

function onDateChange(e) { date.value = e.detail.value }
function onTimeChange(e) { time.value = e.detail.value }
function onModeChange(e) {
  form.value.trialMode = Number(e.detail.value) + 1
  modeText.value = ['线下', '线上'][e.detail.value]
}

async function submit() {
  if (!date.value || !time.value) {
    uni.showToast({ title: '请选择试课时间', icon: 'none' }); return
  }
  if (!form.value.trialPrice) {
    uni.showToast({ title: '请输入试课价格', icon: 'none' }); return
  }

  const trialDate = `${date.value}T${time.value}:00`
  const trialPrice = Math.round(parseFloat(form.value.trialPrice) * 100)

  try {
    // 创建试课
    const res = await uni.request({
      url: '/api/v1/trials',
      method: 'POST',
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` },
      data: {
        orderId: Number(orderId.value),
        trialDate,
        trialDuration: Number(form.value.trialDuration),
        trialPrice,
        trialAddress: form.value.trialAddress,
        trialMode: form.value.trialMode
      }
    })

    if (res.data.code === 200) {
      uni.showToast({ title: '试课邀请已发送', icon: 'success' })
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
.form-group { margin-bottom: 24rpx; }
.label { font-size: 28rpx; color: #333; display: block; margin-bottom: 12rpx; }
.input { background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; }
.picker-text { background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; color: #333; margin-bottom: 12rpx; }
.submit-btn { background: #FF9500; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; margin-top: 30rpx; }
</style>
