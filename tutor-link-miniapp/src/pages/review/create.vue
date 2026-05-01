<template>
  <view class="page">
    <view class="card" v-if="order">
      <text class="card-title">评价订单: {{ order.title }}</text>
    </view>

    <view class="card">
      <text class="label">评分</text>
      <view class="stars">
        <view
          v-for="i in 5" :key="i"
          class="star" @tap="form.rating = i"
        >
          <text :class="i <= form.rating ? 'star-active' : 'star-inactive'">&#9733;</text>
        </view>
      </view>
    </view>

    <view class="card">
      <text class="label">评价内容</text>
      <textarea class="textarea" v-model="form.content" placeholder="说说你的体验..." />
    </view>

    <view class="card">
      <text class="label">标签</text>
      <view class="tags">
        <view
          v-for="tag in tagOptions" :key="tag"
          class="tag" :class="{ 'tag-active': selectedTags.includes(tag) }"
          @tap="toggleTag(tag)"
        >
          <text>{{ tag }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="anon-row" @tap="form.isAnonymous = form.isAnonymous ? 0 : 1">
        <text class="anon-label">匿名评价</text>
        <view class="switch" :class="{ 'switch-on': form.isAnonymous }">
          <view class="switch-dot"></view>
        </view>
      </view>
    </view>

    <button class="submit-btn" @tap="submitReview">提交评价</button>
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

<style scoped>
.page { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 30rpx; font-weight: bold; color: #333; }
.label { font-size: 28rpx; color: #333; display: block; margin-bottom: 16rpx; }
.stars { display: flex; gap: 16rpx; }
.star { padding: 8rpx; }
.star-active { font-size: 48rpx; color: #FFB800; }
.star-inactive { font-size: 48rpx; color: #ddd; }
.textarea { background: #f5f5f5; border-radius: 12rpx; padding: 20rpx; font-size: 28rpx; width: 100%; height: 200rpx; }
.tags { display: flex; flex-wrap: wrap; gap: 16rpx; }
.tag { padding: 12rpx 24rpx; border-radius: 24rpx; background: #f0f0f0; font-size: 26rpx; color: #666; }
.tag-active { background: #E3F2FD; color: #4A90D9; }
.anon-row { display: flex; justify-content: space-between; align-items: center; }
.anon-label { font-size: 28rpx; color: #333; }
.switch { width: 80rpx; height: 44rpx; border-radius: 22rpx; background: #ddd; position: relative; transition: background 0.3s; }
.switch-on { background: #4A90D9; }
.switch-dot { width: 36rpx; height: 36rpx; border-radius: 50%; background: #fff; position: absolute; top: 4rpx; left: 4rpx; transition: left 0.3s; }
.switch-on .switch-dot { left: 40rpx; }
.submit-btn { background: #4A90D9; color: #fff; border-radius: 48rpx; font-size: 32rpx; padding: 24rpx; margin-top: 20rpx; }
</style>
