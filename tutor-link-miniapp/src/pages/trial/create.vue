<template>
  <view class="page">
    <TlCard>
      <text class="form-title">创建试课</text>

      <TlFormInput label="试课日期时间">
        <template #default>
          <picker mode="date" :start="today" @change="onDateChange">
            <view class="picker-text">{{ date || '请选择日期' }}</view>
          </picker>
          <picker mode="time" @change="onTimeChange">
            <view class="picker-text" style="margin-top: 12rpx;">{{ time || '请选择时间' }}</view>
          </picker>
        </template>
      </TlFormInput>

      <TlFormInput label="试课时长(分钟)" v-model="form.trialDuration" type="number" placeholder="如: 60" />
      <TlFormInput label="试课价格(元)" v-model="form.trialPrice" type="digit" placeholder="如: 50" />

      <TlFormInput label="授课方式">
        <template #default>
          <picker :range="['线下', '线上']" @change="onModeChange">
            <view class="picker-text">{{ currentModeText }}</view>
          </picker>
        </template>
      </TlFormInput>

      <TlFormInput v-if="form.trialMode === 1" label="试课地址" v-model="form.trialAddress" placeholder="请输入地址" />

      <TlButton type="warning" @tap="submit" style="margin-top: 30rpx;">发送试课邀请</TlButton>
    </TlCard>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { post } from '../../api/request'

const orderId = ref(null)
const tutorUserId = ref(null)
const date = ref('')
const time = ref('')
const today = ref(new Date().toISOString().split('T')[0])

const form = ref({
  trialDuration: '60',
  trialPrice: '',
  trialAddress: '',
  trialMode: 1
})

const currentModeText = computed(() => {
  return { 1: '线下', 2: '线上' }[form.value.trialMode] || '线下'
})

onLoad((options) => {
  orderId.value = options.orderId
  tutorUserId.value = options.tutorUserId
})

function onDateChange(e) { date.value = e.detail.value }
function onTimeChange(e) { time.value = e.detail.value }
function onModeChange(e) {
  form.value.trialMode = Number(e.detail.value) + 1
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
    await post('/trials', {
      orderId: orderId.value || null,
      tutorUserId: tutorUserId.value,
      trialDate,
      trialDuration: Number(form.value.trialDuration),
      trialPrice,
      trialAddress: form.value.trialAddress,
      trialMode: form.value.trialMode
    })

    uni.showToast({ title: '试课邀请已发送', icon: 'success' })
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

.picker-text {
  background: $color-bg-input;
  border-radius: $radius-md;
  padding: $spacing-lg;
  font-size: $font-size-base;
  color: $color-text-regular;
}
</style>
