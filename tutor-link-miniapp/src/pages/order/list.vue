<template>
  <view class="page">
    <!-- 状态标签 -->
    <scroll-view scroll-x class="tabs">
      <view
        v-for="tab in tabs" :key="tab.value"
        class="tab-item" :class="{ active: currentTab === tab.value }"
        @tap="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </scroll-view>

    <!-- 家教模式: 待接单列表 -->
    <view v-if="isTutor && currentTab === 'pending'">
      <view class="order-card" v-for="item in pendingList" :key="item.id" @tap="goDetail(item.id)">
        <view class="card-header">
          <text class="order-title">{{ item.title }}</text>
          <text class="order-rate">{{ (item.hourlyRate / 100).toFixed(0) }}元/h</text>
        </view>
        <view class="card-info">
          <text>年级: {{ item.grade }}</text>
          <text>方式: {{ modeText(item.teachingMode) }}</text>
        </view>
        <view class="card-info">
          <text>总课时: {{ item.totalHours }}h</text>
          <text>总额: {{ (item.totalAmount / 100).toFixed(2) }}元</text>
        </view>
        <view class="card-footer">
          <text class="card-time">{{ formatTime(item.createTime) }}</text>
          <button class="btn-accept" @tap.stop="handleAccept(item.id)">接单</button>
        </view>
      </view>
    </view>

    <!-- 我的订单列表 -->
    <view v-else>
      <view class="order-card" v-for="item in orderList" :key="item.id" @tap="goDetail(item.id)">
        <view class="card-header">
          <text class="order-title">{{ item.title }}</text>
          <text class="order-status" :class="'status-' + item.status">{{ statusText(item.status) }}</text>
        </view>
        <view class="card-info">
          <text>时薪: {{ (item.hourlyRate / 100).toFixed(0) }}元/h</text>
          <text>方式: {{ modeText(item.teachingMode) }}</text>
        </view>
        <view class="card-info">
          <text>总额: {{ (item.totalAmount / 100).toFixed(2) }}元</text>
        </view>
        <view class="card-footer">
          <text class="card-time">{{ formatTime(item.createTime) }}</text>
          <view class="card-actions" v-if="getActions(item).length">
            <button
              v-for="act in getActions(item)" :key="act.type"
              class="btn-action" :class="'btn-' + act.type"
              size="mini"
              @tap.stop="handleAction(act.type, item)"
            >{{ act.label }}</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && isEmpty" class="empty">
      <text>暂无订单</text>
    </view>

    <!-- 加载更多 -->
    <view v-if="loading" class="loading"><text>加载中...</text></view>
    <view v-if="noMore && !isEmpty" class="no-more"><text>没有更多了</text></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { listMyOrders, listPendingOrders, acceptOrder, cancelOrder, startOrder, completeOrder } from '../../api/order'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isTutor = computed(() => userStore.isTutor())

const tabs = computed(() => {
  if (isTutor.value) {
    return [
      { label: '待接单', value: 'pending' },
      { label: '全部', value: 'all' },
      { label: '试课中', value: 10 },
      { label: '待支付', value: 2 },
      { label: '进行中', value: 4 },
      { label: '已完成', value: 5 }
    ]
  }
  return [
    { label: '全部', value: 'all' },
    { label: '待确认', value: 1 },
    { label: '试课中', value: 10 },
    { label: '待支付', value: 2 },
    { label: '进行中', value: 4 },
    { label: '已完成', value: 5 }
  ]
})

const currentTab = ref('all')
const orderList = ref([])
const pendingList = ref([])
const loading = ref(false)
const page = ref(1)
const noMore = ref(false)

const isEmpty = computed(() => {
  if (currentTab.value === 'pending') return pendingList.value.length === 0
  return orderList.value.length === 0
})

onShow(() => {
  if (!userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loadData(true)
})

function switchTab(val) {
  currentTab.value = val
  loadData(true)
}

async function loadData(reset = false) {
  if (loading.value) return
  if (reset) {
    page.value = 1
    noMore.value = false
    orderList.value = []
    pendingList.value = []
  }
  loading.value = true
  try {
    if (currentTab.value === 'pending') {
      const data = await listPendingOrders({ page: page.value, size: 20 })
      if (reset) pendingList.value = data.records || []
      else pendingList.value.push(...(data.records || []))
      noMore.value = !data.records || data.records.length < 20
    } else {
      const params = { page: page.value, size: 20, role: userStore.role }
      if (currentTab.value !== 'all') params.status = currentTab.value
      const data = await listMyOrders(params)
      if (reset) orderList.value = data.records || []
      else orderList.value.push(...(data.records || []))
      noMore.value = !data.records || data.records.length < 20
    }
    page.value++
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

// 触底加载更多
onReachBottom(() => {
  if (!noMore.value && !loading.value) loadData()
})

function goDetail(id) {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

function statusText(s) {
  const map = { 1: '待确认', 2: '待支付', 3: '已支付', 4: '进行中', 5: '已完成', 6: '已取消', 7: '退款中', 8: '已退款', 9: '争议中', 10: '试课中' }
  return map[s] || '未知'
}

function modeText(m) {
  return { 1: '线下', 2: '线上', 3: '均可' }[m] || '未知'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function getActions(item) {
  const actions = []
  if (isTutor.value) {
    if (item.status === 2) actions.push({ type: 'start', label: '开始上课' })
    if (item.status === 3) actions.push({ type: 'start', label: '开始上课' })
  } else {
    if (item.status === 2) actions.push({ type: 'pay', label: '去支付' })
    if (item.status === 4) actions.push({ type: 'complete', label: '确认完成' })
  }
  if ([1, 2, 3].includes(item.status)) {
    actions.push({ type: 'cancel', label: '取消' })
  }
  return actions
}

async function handleAction(type, item) {
  try {
    if (type === 'accept') {
      await acceptOrder(item.id)
      uni.showToast({ title: '接单成功', icon: 'success' })
    } else if (type === 'start') {
      await startOrder(item.id)
      uni.showToast({ title: '已开始上课', icon: 'success' })
    } else if (type === 'complete') {
      await completeOrder(item.id)
      uni.showToast({ title: '已确认完成', icon: 'success' })
    } else if (type === 'cancel') {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            await cancelOrder(item.id, { cancelReason: '用户主动取消', role: userStore.role })
            uni.showToast({ title: '已取消', icon: 'success' })
            loadData(true)
          }
        }
      })
      return
    } else if (type === 'pay') {
      uni.navigateTo({ url: `/pages/order/detail?id=${item.id}&action=pay` })
      return
    }
    loadData(true)
  } catch (e) { console.error(e) }
}

async function handleAccept(id) {
  try {
    await acceptOrder(id)
    uni.showToast({ title: '接单成功', icon: 'success' })
    loadData(true)
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.page { padding: 0 20rpx 20rpx; background: #f5f5f5; min-height: 100vh; }
.tabs { white-space: nowrap; background: #fff; padding: 20rpx; margin-bottom: 20rpx; border-radius: 0 0 16rpx 16rpx; }
.tab-item { display: inline-block; padding: 12rpx 28rpx; margin-right: 16rpx; border-radius: 32rpx; font-size: 26rpx; color: #666; background: #f0f0f0; }
.tab-item.active { background: #4A90D9; color: #fff; }
.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.order-title { font-size: 30rpx; font-weight: bold; color: #333; flex: 1; }
.order-rate { font-size: 30rpx; color: #E86C3A; font-weight: bold; }
.order-status { font-size: 24rpx; padding: 4rpx 16rpx; border-radius: 16rpx; }
.status-1 { background: #FFF3E0; color: #E86C3A; }
.status-2 { background: #E3F2FD; color: #4A90D9; }
.status-3 { background: #E8F5E9; color: #4CAF50; }
.status-4 { background: #E8F5E9; color: #4CAF50; }
.status-5 { background: #F5F5F5; color: #999; }
.status-6 { background: #FFEBEE; color: #F44336; }
.status-7 { background: #FFF3E0; color: #FF9800; }
.status-8 { background: #F5F5F5; color: #999; }
.status-9 { background: #FFEBEE; color: #F44336; }
.card-info { display: flex; gap: 24rpx; font-size: 26rpx; color: #666; margin-bottom: 8rpx; }
.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f0f0f0; }
.card-time { font-size: 22rpx; color: #999; }
.card-actions { display: flex; gap: 12rpx; }
.btn-action { font-size: 24rpx; padding: 8rpx 20rpx; border-radius: 24rpx; }
.btn-pay { background: #4A90D9; color: #fff; }
.btn-start { background: #4CAF50; color: #fff; }
.btn-complete { background: #4CAF50; color: #fff; }
.btn-cancel { background: #fff; color: #999; border: 1rpx solid #ddd; }
.btn-accept { background: #4A90D9; color: #fff; font-size: 26rpx; padding: 8rpx 28rpx; border-radius: 24rpx; }
.empty { text-align: center; padding: 120rpx; color: #999; font-size: 28rpx; }
.loading, .no-more { text-align: center; padding: 20rpx; color: #999; font-size: 24rpx; }
</style>
