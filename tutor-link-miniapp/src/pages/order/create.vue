<template>
  <view class="page">
    <view class="form-card">
      <text class="form-title">发布家教需求</text>
      <view class="form-group">
        <text class="label">标题</text>
        <input class="input" v-model="form.title" placeholder="简要描述需求" />
      </view>
      <view class="form-group">
        <text class="label">科目</text>
        <picker :range="subjectNames" @change="onSubjectChange">
          <view class="picker-text">{{ form.subjectName || '请选择科目' }}</view>
        </picker>
      </view>
      <view class="form-group">
        <text class="label">年级</text>
        <input class="input" v-model="form.grade" placeholder="如: 高二" />
      </view>
      <view class="form-group">
        <text class="label">时薪(元)</text>
        <input class="input" v-model="form.hourlyRate" type="digit" placeholder="如: 80" />
      </view>
      <view class="form-group">
        <text class="label">总课时</text>
        <input class="input" v-model="form.totalHours" type="number" placeholder="如: 20" />
      </view>
      <view class="form-group">
        <text class="label">授课方式</text>
        <picker :range="['线下', '线上', '均可']" @change="onModeChange">
          <view class="picker-text">{{ modeText }}</view>
        </picker>
      </view>
      <view class="form-group">
        <text class="label">详细描述</text>
        <textarea class="textarea" v-model="form.description" placeholder="描述具体需求" />
      </view>
      <button class="submit-btn" @tap="submitOrder">发布需求</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createOrder } from '../../api/order'
import { sendMessage } from '../../api/chat'
import { get } from '../../api/request'

const tutorUserId = ref(null)

const form = ref({
  title: '', subjectId: null, subjectName: '',
  grade: '', hourlyRate: '', totalHours: '',
  teachingMode: 1, description: ''
})
const subjectNames = ref([])
const subjectIds = ref([])
const modeText = ref('线下')

onLoad((options) => {
  if (options.tutorUserId) {
    tutorUserId.value = Number(options.tutorUserId)
  }
  loadSubjects()
})

async function loadSubjects() {
  try {
    const data = await get('/subjects')
    subjectNames.value = data.map(s => s.name)
    subjectIds.value = data.map(s => s.id)
  } catch (e) { console.error(e) }
}

function onSubjectChange(e) {
  form.value.subjectId = subjectIds.value[e.detail.value]
  form.value.subjectName = subjectNames.value[e.detail.value]
}

function onModeChange(e) {
  form.value.teachingMode = e.detail.value + 1
  modeText.value = ['线下', '线上', '均可'][e.detail.value]
}

async function submitOrder() {
  if (!form.value.title || !form.value.subjectId || !form.value.grade) {
    uni.showToast({ title: '请填写必要信息', icon: 'none' })
    return
  }
  try {
    const order = await createOrder({
      title: form.value.title,
      subjectId: form.value.subjectId,
      grade: form.value.grade,
      hourlyRate: Math.round(parseFloat(form.value.hourlyRate) * 100),
      totalHours: parseInt(form.value.totalHours),
      teachingMode: form.value.teachingMode,
      description: form.value.description
    })

    // 如果是从聊天页面发起的，发送订单卡片消息给家教
    if (tutorUserId.value) {
      try {
        await sendMessage(tutorUserId.value, 3, JSON.stringify({
          orderId: order.id,
          title: form.value.title,
          grade: form.value.grade,
          hourlyRate: Math.round(parseFloat(form.value.hourlyRate) * 100),
          totalHours: parseInt(form.value.totalHours)
        }))
      } catch (e) { console.error('Send order card failed', e) }
      uni.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1500)
    } else {
      uni.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => uni.switchTab({ url: '/pages/order/list' }), 1500)
    }
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.page { padding: 20rpx; }
.form-card { background: #fff; border-radius: 16rpx; padding: 30rpx; }
.form-title { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 30rpx; }
.form-group { margin-bottom: 24rpx; }
.label { font-size: 28rpx; color: #333; display: block; margin-bottom: 12rpx; }
.input { background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; }
.picker-text { background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; color: #333; }
.textarea { background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; width: 100%; height: 200rpx; }
.submit-btn { background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; margin-top: 30rpx; }
</style>
