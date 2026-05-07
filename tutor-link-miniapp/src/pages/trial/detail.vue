<template>
  <view class="page" v-if="trial">
    <TlCard>
      <text class="card-title">试课信息</text>
      <TlInfoRow label="状态">
        <template #value>
          <TlStatusBadge :status="trial.status" type="trial" />
        </template>
      </TlInfoRow>
      <TlInfoRow label="试课时间" :value="formatTime(trial.trialDate)" />
      <TlInfoRow label="试课时长" :value="trial.trialDuration + '分钟'" />
      <TlInfoRow label="试课价格" :value="'¥' + (trial.trialPrice / 100).toFixed(0)" type="price" />
      <TlInfoRow label="授课方式" :value="trial.trialMode === 1 ? '线下' : '线上'" />
      <TlInfoRow v-if="trial.trialAddress" label="地址" :value="trial.trialAddress" />
      <TlInfoRow v-if="trial.parentFeedback" label="家长反馈" :value="trial.parentFeedback" />
    </TlCard>

    <!-- 老师确认试课 -->
    <TlActionBar v-if="!isParent && trial.status === 1">
      <TlButton @tap="confirmTrial">确认试课</TlButton>
    </TlActionBar>

    <!-- 家长评价试课 -->
    <TlActionBar v-if="isParent && trial.status === 2">
      <TlButton type="success" @tap="evaluate(1)">试课通过</TlButton>
      <TlButton type="danger-outline" @tap="evaluate(2)">试课不通过</TlButton>
    </TlActionBar>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'
import { get, put } from '../../api/request'
import { formatTime } from '../../utils/formatters'

const userStore = useUserStore()
const isParent = userStore.isParent()
const trial = ref(null)

onLoad(async (options) => {
  await loadTrial(options.id)
})

async function loadTrial(id) {
  try {
    trial.value = await get(`/trials/${id}`)
  } catch (e) { console.error(e) }
}

async function confirmTrial() {
  try {
    await put(`/trials/${trial.value.id}/confirm`)
    uni.showToast({ title: '已确认', icon: 'success' })
    loadTrial(trial.value.id)
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
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
    await put(`/trials/${trial.value.id}/evaluate`, { result, feedback })
    uni.showToast({ title: result === 1 ? '试课通过' : '已取消', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
}
</script>

<style lang="scss" scoped>
.page {
  padding: $spacing-page;
  padding-bottom: 140rpx;
}

.card-title {
  font-size: $font-size-md;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-md;
}
</style>
