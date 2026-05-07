<template>
  <view class="page">
    <!-- 状态标签 -->
    <TlScrollTabs :tabs="tabs" v-model="currentTab" />

    <!-- 家教模式: 待接单列表 -->
    <view v-if="isTutor && currentTab === 'pending'">
      <TlCard v-for="item in pendingList" :key="item.id" @tap="goDetail(item.id)">
        <view class="card-header">
          <text class="order-title">{{ item.title }}</text>
          <text class="order-rate">{{ formatPrice(item.hourlyRate) }}/h</text>
        </view>
        <view class="card-info">
          <text>年级: {{ item.grade }}</text>
          <text>方式: {{ modeText(item.teachingMode) }}</text>
        </view>
        <view class="card-info">
          <text>总课时: {{ item.totalHours }}h</text>
          <text>总额: {{ formatPrice(item.totalAmount) }}</text>
        </view>
        <view class="card-footer">
          <text class="card-time">{{ formatTime(item.createTime) }}</text>
          <TlButton type="primary" size="small" @tap.stop="handleInterest(item.id)">感兴趣</TlButton>
        </view>
      </TlCard>
    </view>

    <!-- 我的订单列表 -->
    <view v-else>
      <TlCard v-for="item in orderList" :key="item.id" @tap="goDetail(item.id)">
        <view class="card-header">
          <text class="order-title">{{ item.title }}</text>
          <TlStatusBadge :status="item.status" type="order" />
        </view>
        <view class="card-info">
          <text>时薪: {{ formatPrice(item.hourlyRate) }}/h</text>
          <text>方式: {{ modeText(item.teachingMode) }}</text>
        </view>
        <view class="card-info">
          <text>总额: {{ formatPrice(item.totalAmount) }}</text>
        </view>
        <view class="card-footer">
          <text class="card-time">{{ formatTime(item.createTime) }}</text>
          <view class="card-actions" v-if="getActions(item).length">
            <TlButton
              v-for="act in getActions(item)" :key="act.type"
              :type="actBtnType(act.type)"
              size="small"
              @tap.stop="handleAction(act.type, item)"
            >{{ act.label }}</TlButton>
          </view>
        </view>
      </TlCard>
    </view>

    <!-- 空状态 -->
    <TlEmpty v-if="!loading && isEmpty" icon="📋" text="暂无订单" />
    <TlLoading v-if="loading" text="加载中..." />
    <view v-if="noMore && !isEmpty" class="no-more"><text>没有更多了</text></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { listMyOrders, listPendingOrders, expressInterest, confirmDelegation, cancelOrder, startOrder, completeOrder } from '../../api/order'
import { useUserStore } from '../../store/user'
import { orderStatusText, modeText, formatTime, formatPrice } from '../../utils/formatters'

const userStore = useUserStore()
const isTutor = computed(() => userStore.isTutor())

const tabs = computed(() => {
  if (isTutor.value) {
    return [
      { label: '待接单', value: 'pending' },
      { label: '全部', value: 'all' },
      { label: '有意向', value: 11 },
      { label: '试课中', value: 10 },
      { label: '待支付', value: 2 },
      { label: '进行中', value: 4 },
      { label: '已完成', value: 5 }
    ]
  }
  return [
    { label: '全部', value: 'all' },
    { label: '待确认', value: 1 },
    { label: '有意向', value: 11 },
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

onReachBottom(() => {
  if (!noMore.value && !loading.value) loadData()
})

function goDetail(id) {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

function getActions(item) {
  const actions = []
  if (isTutor.value) {
    if (item.status === 2 || item.status === 3) actions.push({ type: 'start', label: '开始上课' })
  } else {
    if (item.status === 11) actions.push({ type: 'confirm', label: '确认委托' })
    if (item.status === 2) actions.push({ type: 'pay', label: '去支付' })
    if (item.status === 4) actions.push({ type: 'complete', label: '确认完成' })
  }
  if ([1, 11, 2, 3].includes(item.status)) {
    actions.push({ type: 'cancel', label: '取消' })
  }
  return actions
}

function actBtnType(type) {
  const map = { pay: 'primary', start: 'success', complete: 'success', cancel: 'danger-outline', confirm: 'warning' }
  return map[type] || 'primary'
}

async function handleAction(type, item) {
  try {
    if (type === 'confirm') {
      await confirmDelegation(item.id)
      uni.showToast({ title: '委托成功', icon: 'success' })
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

async function handleInterest(id) {
  try {
    await expressInterest(id)
    uni.showToast({ title: '已表达意向', icon: 'success' })
    loadData(true)
  } catch (e) { console.error(e) }
}
</script>

<style lang="scss" scoped>
.page {
  padding: 0 $spacing-page $spacing-page;
  background: $color-bg-page;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.order-title {
  font-size: $font-size-md;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  flex: 1;
}

.order-rate {
  font-size: $font-size-md;
  color: $color-warning;
  font-weight: $font-weight-bold;
}

.card-info {
  display: flex;
  gap: $spacing-lg;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-bottom: $spacing-xs;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $color-border;
}

.card-time {
  font-size: $font-size-xs;
  color: $color-text-secondary;
}

.card-actions {
  display: flex;
  gap: $spacing-sm;
}

.no-more {
  text-align: center;
  padding: $spacing-md 0;
  color: $color-text-placeholder;
  font-size: $font-size-sm;
}
</style>
