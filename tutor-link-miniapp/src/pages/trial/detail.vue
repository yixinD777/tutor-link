<template>
  <view class="page" v-if="trial">
    <view class="card">
      <view class="card-title">试课信息</view>
      <view class="info-row">
        <text class="label">状态</text>
        <text class="value" :class="'status-' + trial.status">{{ statusText(trial.status) }}</text>
      </view>
      <view class="info-row">
        <text class="label">试课时间</text>
        <text class="value">{{ formatTime(trial.trialDate) }}</text>
      </view>
      <view class="info-row">
        <text class="label">试课时长</text>
        <text class="value">{{ trial.trialDuration }}分钟</text>
      </view>
      <view class="info-row">
        <text class="label">试课价格</text>
        <text class="value price">¥{{ (trial.trialPrice / 100).toFixed(0) }}</text>
      </view>
      <view class="info-row">
        <text class="label">授课方式</text>
        <text class="value">{{ trial.trialMode === 1 ? '线下' : '线上' }}</text>
      </view>
      <view class="info-row" v-if="trial.trialAddress">
        <text class="label">地址</text>
        <text class="value">{{ trial.trialAddress }}</text>
      </view>
      <view class="info-row" v-if="trial.parentFeedback">
        <text class="label">家长反馈</text>
        <text class="value">{{ trial.parentFeedback }}</text>
      </view>
    </view>

    <!-- 老师确认试课 -->
    <view v-if="!isParent && trial.status === 1" class="action-bar">
      <button class="confirm-btn" @tap="confirmTrial">确认试课</button>
    </view>

    <!-- 家长评价试课 -->
    <view v-if="isParent && trial.status === 2" class="action-bar">
      <button class="pass-btn" @tap="evaluate(1)">试课通过</button>
      <button class="fail-btn" @tap="evaluate(2)">试课不通过</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isParent = userStore.isParent()
const trial = ref(null)

onLoad(async (options) => {
  await loadTrial(options.id)
})

async function loadTrial(id) {
  try {
    const res = await uni.request({
      url: `/api/v1/trials/${id}`,
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` }
    })
    if (res.data.code === 200) trial.value = res.data.data
  } catch (e) { console.error(e) }
}

function statusText(s) {
  return { 1: '待确认', 2: '已确认', 3: '已完成', 4: '已取消' }[s] || '未知'
}

function formatTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '' }

async function confirmTrial() {
  try {
    const res = await uni.request({
      url: `/api/v1/trials/${trial.value.id}/confirm`,
      method: 'PUT',
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` }
    })
    if (res.data.code === 200) {
      uni.showToast({ title: '已确认', icon: 'success' })
      loadTrial(trial.value.id)
    } else {
      uni.showToast({ title: res.data.message, icon: 'none' })
    }
  } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

async function evaluate(result) {
  const feedback = result === 1 ? '' : await new Promise(resolve => {
    uni.showModal({
      title: '试课反馈',
      editable: true,
      placeholderText: '请输入反馈（选填）',
      success: (res) => resolve(res.content || '')
    })
  })
  try {
    const res = await uni.request({
      url: `/api/v1/trials/${trial.value.id}/evaluate`,
      method: 'PUT',
      header: { 'Authorization': `Bearer ${uni.getStorageSync('accessToken')}` },
      data: { result, feedback }
    })
    if (res.data.code === 200) {
      uni.showToast({ title: result === 1 ? '试课通过' : '已取消', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1500)
    } else {
      uni.showToast({ title: res.data.message, icon: 'none' })
    }
  } catch (e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
}
</script>

<style scoped>
.page { padding: 20rpx; padding-bottom: 140rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.info-row { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.label { font-size: 28rpx; color: #666; }
.value { font-size: 28rpx; color: #333; }
.price { color: #FF9500; font-weight: bold; }
.status-1 { color: #FF9500; }
.status-2 { color: #4A90D9; }
.status-3 { color: #07C160; }
.status-4 { color: #999; }
.action-bar { position: fixed; bottom: 0; left: 0; right: 0; display: flex; padding: 20rpx; background: #fff; gap: 20rpx; }
.confirm-btn { flex: 1; background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 30rpx; padding: 20rpx; }
.pass-btn { flex: 1; background: #07C160; color: #fff; border-radius: 48rpx; font-size: 30rpx; padding: 20rpx; }
.fail-btn { flex: 1; background: #fff; color: #ff4d4f; border: 2rpx solid #ff4d4f; border-radius: 48rpx; font-size: 30rpx; padding: 20rpx; }
</style>
