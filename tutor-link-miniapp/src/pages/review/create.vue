<template>
  <view class="page">
    <TlCard v-if="order">
      <text class="card-title">评价订单: {{ order.title }}</text>
    </TlCard>

    <TlCard>
      <text class="label">评分</text>
      <TlRating :value="form.rating" :editable="true" @update:value="form.rating = $event" size="large" />
    </TlCard>

    <TlCard>
      <TlFormTextarea label="评价内容" v-model="form.content" placeholder="说说你的体验..." />
    </TlCard>

    <TlCard>
      <text class="label">标签</text>
      <view class="tags">
        <view
          v-for="tag in tagOptions" :key="tag"
          :class="['tl-tag', { 'tl-tag--active': selectedTags.includes(tag) }]"
          @tap="toggleTag(tag)"
        >
          <text>{{ tag }}</text>
        </view>
      </view>
    </TlCard>

    <TlCard>
      <view class="anon-row" @tap="form.isAnonymous = form.isAnonymous ? 0 : 1">
        <text class="anon-label">匿名评价</text>
        <TlSwitch :modelValue="!!form.isAnonymous" @update:modelValue="form.isAnonymous = $event ? 1 : 0" />
      </view>
    </TlCard>

    <TlButton @tap="submitReview" style="margin-top: 20rpx;">提交评价</TlButton>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrder } from '../../api/order'
import { createReview } from '../../api/review'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const orderId = ref(null)
const order = ref(null)
const selectedTags = ref([])

const tagOptions = ['耐心细致', '讲解清晰', '准时守信', '准备充分', '善于引导', '态度友好', '专业扎实', '方法灵活']

const form = ref({
  rating: 5,
  content: '',
  tags: '',
  isAnonymous: 0
})

onLoad((options) => {
  if (options.orderId) {
    orderId.value = options.orderId
    loadOrder()
  }
})

async function loadOrder() {
  try {
    order.value = await getOrder(orderId.value)
  } catch (e) { console.error(e) }
}

function toggleTag(tag) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else selectedTags.value.push(tag)
}

async function submitReview() {
  if (!orderId.value) {
    uni.showToast({ title: '订单信息缺失', icon: 'none' })
    return
  }
  try {
    await createReview({
      orderId: orderId.value,
      rating: form.value.rating,
      content: form.value.content,
      tags: selectedTags.value.join(','),
      isAnonymous: form.value.isAnonymous,
      role: userStore.role
    })
    uni.showToast({ title: '评价成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) { console.error(e) }
}
</script>

<style lang="scss" scoped>
.page {
  padding: $spacing-page;
  background: $color-bg-page;
  min-height: 100vh;
}

.card-title {
  font-size: $font-size-md;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.label {
  font-size: $font-size-base;
  color: $color-text-regular;
  display: block;
  margin-bottom: $spacing-md;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.tl-tag {
  padding: $spacing-sm $spacing-lg;
  border-radius: $radius-pill;
  background: $color-bg-input;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  transition: all $duration-fast $ease-default;

  &--active {
    background: $color-primary-light;
    color: $color-primary;
  }
}

.anon-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.anon-label {
  font-size: $font-size-base;
  color: $color-text-regular;
}
</style>
