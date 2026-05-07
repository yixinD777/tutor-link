<template>
  <view class="page">
    <TlCard>
      <text class="form-title">发布家教需求</text>
      <TlFormInput label="标题" v-model="form.title" placeholder="简要描述需求" required />

      <TlFormInput label="科目">
        <template #default>
          <picker :range="subjectNames" @change="onSubjectChange">
            <view class="picker-text">{{ form.subjectName || '请选择科目' }}</view>
          </picker>
        </template>
      </TlFormInput>

      <TlFormInput label="年级" v-model="form.grade" placeholder="如: 高二" />
      <TlFormInput label="时薪(元)" v-model="form.hourlyRate" type="digit" placeholder="如: 80" />
      <TlFormInput label="总课时" v-model="form.totalHours" type="number" placeholder="如: 20" />

      <TlFormInput label="授课方式">
        <template #default>
          <picker :range="['线下', '线上', '均可']" @change="onModeChange">
            <view class="picker-text">{{ currentModeText }}</view>
          </picker>
        </template>
      </TlFormInput>

      <TlFormTextarea label="详细描述" v-model="form.description" placeholder="描述具体需求" />

      <TlButton @tap="submitOrder" style="margin-top: 30rpx;">发布需求</TlButton>
    </TlCard>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
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

const currentModeText = computed(() => {
  return { 1: '线下', 2: '线上', 3: '均可' }[form.value.teachingMode] || '线下'
})

onLoad((options) => {
  if (options.tutorUserId) {
    tutorUserId.value = options.tutorUserId
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
