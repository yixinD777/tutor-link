<template>
  <view class="page" v-if="order">
    <!-- 订单状态 -->
    <view :class="['status-bar', `status-bg-${order.status}`]">
      <text class="status-main">{{ orderStatusText(order.status) }}</text>
      <text class="status-sub">{{ orderStatusDesc(order.status) }}</text>
    </view>

    <TlCard>
      <text class="card-title">需求信息</text>
      <TlInfoRow label="标题" :value="order.title" />
      <TlInfoRow label="年级" :value="order.grade" />
      <TlInfoRow label="授课方式" :value="modeText(order.teachingMode)" />
      <TlInfoRow v-if="order.teachingAddress" label="授课地址" :value="order.teachingAddress" />
      <TlInfoRow label="时薪" :value="(order.hourlyRate / 100).toFixed(0) + '元/h'" type="price" />
      <TlInfoRow label="总课时" :value="order.totalHours + '小时'" />
      <TlInfoRow label="总额" :value="(order.totalAmount / 100).toFixed(2) + '元'" type="price" />
      <TlInfoRow v-if="order.description" label="描述" :value="order.description" />
    </TlCard>

    <TlCard>
      <text class="card-title">时间信息</text>
      <TlInfoRow label="创建时间" :value="formatTime(order.createTime)" />
      <TlInfoRow v-if="order.confirmTime" label="确认时间" :value="formatTime(order.confirmTime)" />
      <TlInfoRow v-if="order.startTime" label="开始时间" :value="formatTime(order.startTime)" />
      <TlInfoRow v-if="order.completeTime" label="完成时间" :value="formatTime(order.completeTime)" />
      <TlInfoRow v-if="order.cancelReason" label="取消原因" :value="order.cancelReason" type="cancel" />
    </TlCard>

    <TlCard>
      <text class="card-title">流转记录</text>
      <TlTimeline v-if="logs.length" :items="logItems" />
      <TlEmpty v-else icon="📋" text="暂无记录" />
    </TlCard>

    <TlActionBar v-if="actions.length">
      <TlButton v-for="act in actions" :key="act.type" :type="actBtnType(act.type)" @tap="handleAction(act.type)">{{ act.label }}</TlButton>
    </TlActionBar>
  </view>

  <TlLoading v-else mode="skeleton" :rows="4" />
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrder, getOrderLogs, expressInterest, confirmDelegation, cancelOrder, startOrder, completeOrder, prepay } from '../../api/order'
import { useUserStore } from '../../store/user'
import { orderStatusText, orderStatusDesc, modeText, formatTime } from '../../utils/formatters'

const userStore = useUserStore()
const orderId = ref(null)
const order = ref(null)
const logs = ref([])

onLoad((options) => {
  orderId.value = options.id
  loadDetail()
  if (options.action === 'pay') setTimeout(() => handleAction('pay'), 500)
})

async function loadDetail() {
  try {
    order.value = await getOrder(orderId.value)
    const data = await getOrderLogs(orderId.value)
    logs.value = data || []
  } catch (e) { console.error(e) }
}

const isTutor = computed(() => userStore.isTutor())
const isParent = computed(() => userStore.isParent())
const logItems = computed(() => logs.value.map(l => ({ action: l.action, time: formatTime(l.createTime) })))

const actions = computed(() => {
  if (!order.value) return []
  const s = order.value.status
  const list = []
  if (isTutor.value) {
    if (s === 1) list.push({ type: 'interest', label: '感兴趣' })
    if (s === 2 || s === 3) list.push({ type: 'start', label: '开始上课' })
  }
  if (isParent.value) {
    if (s === 11) list.push({ type: 'confirm', label: '确认委托' })
    if (s === 2) list.push({ type: 'pay', label: '去支付' })
    if (s === 2) list.push({ type: 'trial', label: '创建试课' })
    if (s === 4) list.push({ type: 'complete', label: '确认完成' })
    if (s === 3 || s === 4) list.push({ type: 'schedule', label: '课程排期' })
  }
  if ([1, 11, 2, 3].includes(s)) list.push({ type: 'cancel', label: '取消订单' })
  return list
})

function actBtnType(type) {
  const map = { interest: 'primary', confirm: 'warning', pay: 'warning', start: 'success', complete: 'success', cancel: 'danger-outline' }
  return map[type] || 'primary'
}

async function handleAction(type) {
  try {
    if (type === 'interest') { await expressInterest(orderId.value); uni.showToast({ title: '已表达意向', icon: 'success' }) }
    else if (type === 'confirm') { await confirmDelegation(orderId.value); uni.showToast({ title: '委托成功', icon: 'success' }) }
    else if (type === 'start') { await startOrder(orderId.value); uni.showToast({ title: '已开始上课', icon: 'success' }) }
    else if (type === 'complete') { await completeOrder(orderId.value); uni.showToast({ title: '已确认完成', icon: 'success' }) }
    else if (type === 'cancel') {
      uni.showModal({ title: '确认取消', content: '确定要取消该订单吗？', success: async (res) => {
        if (res.confirm) { await cancelOrder(orderId.value, { cancelReason: '用户主动取消', role: userStore.role }); uni.showToast({ title: '已取消', icon: 'success' }); loadDetail() }
      }})
      return
    } else if (type === 'pay') {
      uni.showModal({ title: '确认支付', content: `支付金额: ${(order.value.totalAmount / 100).toFixed(2)}元`, success: async (res) => {
        if (res.confirm) { try { await prepay(orderId.value); uni.showToast({ title: '支付成功', icon: 'success' }); loadDetail() } catch (e) { uni.showToast({ title: e.message || '支付失败', icon: 'none' }) } }
      }})
      return
    } else if (type === 'trial') { uni.navigateTo({ url: `/pages/trial/create?orderId=${orderId.value}&tutorUserId=${order.value.tutorUserId}` }); return }
    else if (type === 'schedule') { uni.navigateTo({ url: `/pages/schedule/create?orderId=${orderId.value}&address=${order.value.teachingAddress || ''}&mode=${order.value.teachingMode || 1}` }); return }
    loadDetail()
  } catch (e) { console.error(e) }
}
</script>

<style lang="scss" scoped>
.page { padding-bottom: 140rpx; background: $color-bg-page; min-height: 100vh; animation: fadeIn 0.2s $ease-default; }
.status-bar { padding: $spacing-2xl $spacing-page; color: #fff; border-radius: 0 0 $radius-2xl $radius-2xl;
  &.status-bg-1 { background: $color-warning; }
  &.status-bg-11 { background: $color-primary; }
  &.status-bg-2 { background: $color-primary; }
  &.status-bg-3 { background: $color-success; }
  &.status-bg-4 { background: $color-success; }
  &.status-bg-5 { background: $color-text-secondary; }
  &.status-bg-6 { background: $color-danger; }
  &.status-bg-10 { background: $color-warning; }
  &.status-bg-7 { background: $color-warning; }
  &.status-bg-8 { background: $color-text-secondary; }
  &.status-bg-9 { background: $color-danger; }
}
.status-main { font-size: $font-size-xl; font-weight: $font-weight-bold; display: block; }
.status-sub { font-size: $font-size-sm; opacity: 0.9; display: block; margin-top: $spacing-xs; }
.card-title { font-size: $font-size-md; font-weight: $font-weight-bold; color: $color-text-primary; margin-bottom: $spacing-md; padding-bottom: $spacing-sm; border-bottom: 1rpx solid $color-border; }
</style>
