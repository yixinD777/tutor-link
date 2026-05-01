<template>
  <view class="page" v-if="order">
    <!-- 订单状态 -->
    <view class="status-bar" :class="'status-bg-' + order.status">
      <text class="status-main">{{ statusText(order.status) }}</text>
      <text class="status-sub">{{ statusDesc(order.status) }}</text>
    </view>

    <!-- 订单信息 -->
    <view class="card">
      <view class="card-title">需求信息</view>
      <view class="info-row">
        <text class="info-label">标题</text>
        <text class="info-value">{{ order.title }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">年级</text>
        <text class="info-value">{{ order.grade }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">授课方式</text>
        <text class="info-value">{{ modeText(order.teachingMode) }}</text>
      </view>
      <view class="info-row" v-if="order.teachingAddress">
        <text class="info-label">授课地址</text>
        <text class="info-value">{{ order.teachingAddress }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">时薪</text>
        <text class="info-value price">{{ (order.hourlyRate / 100).toFixed(0) }}元/h</text>
      </view>
      <view class="info-row">
        <text class="info-label">总课时</text>
        <text class="info-value">{{ order.totalHours }}小时</text>
      </view>
      <view class="info-row">
        <text class="info-label">总额</text>
        <text class="info-value price">{{ (order.totalAmount / 100).toFixed(2) }}元</text>
      </view>
      <view class="info-row" v-if="order.description">
        <text class="info-label">描述</text>
        <text class="info-value desc">{{ order.description }}</text>
      </view>
    </view>

    <!-- 时间信息 -->
    <view class="card">
      <view class="card-title">时间信息</view>
      <view class="info-row">
        <text class="info-label">创建时间</text>
        <text class="info-value">{{ formatTime(order.createTime) }}</text>
      </view>
      <view class="info-row" v-if="order.confirmTime">
        <text class="info-label">确认时间</text>
        <text class="info-value">{{ formatTime(order.confirmTime) }}</text>
      </view>
      <view class="info-row" v-if="order.startTime">
        <text class="info-label">开始时间</text>
        <text class="info-value">{{ formatTime(order.startTime) }}</text>
      </view>
      <view class="info-row" v-if="order.completeTime">
        <text class="info-label">完成时间</text>
        <text class="info-value">{{ formatTime(order.completeTime) }}</text>
      </view>
      <view class="info-row" v-if="order.cancelReason">
        <text class="info-label">取消原因</text>
        <text class="info-value cancel">{{ order.cancelReason }}</text>
      </view>
    </view>

    <!-- 状态流转日志 -->
    <view class="card">
      <view class="card-title">流转记录</view>
      <view class="log-item" v-for="log in logs" :key="log.id">
        <view class="log-dot"></view>
        <view class="log-content">
          <text class="log-action">{{ log.action }}</text>
          <text class="log-time">{{ formatTime(log.createTime) }}</text>
        </view>
      </view>
      <view v-if="!logs.length" class="empty-logs"><text>暂无记录</text></view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="actions.length">
      <button
        v-for="act in actions" :key="act.type"
        class="bottom-btn" :class="'btn-' + act.type"
        @tap="handleAction(act.type)"
      >{{ act.label }}</button>
    </view>
  </view>

  <view v-else class="page loading"><text>加载中...</text></view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { onLoad } from '@dcloudio/uni-app'
import { getOrder, getOrderLogs, acceptOrder, cancelOrder, startOrder, completeOrder, prepay } from '../../api/order'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const orderId = ref(null)
const order = ref(null)
const logs = ref([])

onLoad((options) => {
  orderId.value = options.id
  loadDetail()
  if (options.action === 'pay') {
    // 自动触发支付
    setTimeout(() => handleAction('pay'), 500)
  }
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

const actions = computed(() => {
  if (!order.value) return []
  const s = order.value.status
  const list = []
  if (isTutor.value) {
    if (s === 1) list.push({ type: 'accept', label: '接单' })
    if (s === 2 || s === 3) list.push({ type: 'start', label: '开始上课' })
  }
  if (isParent.value) {
    if (s === 2) list.push({ type: 'pay', label: '去支付' })
    if (s === 4) list.push({ type: 'complete', label: '确认完成' })
  }
  if ([1, 2, 3].includes(s)) list.push({ type: 'cancel', label: '取消订单' })
  return list
})

function statusText(s) {
  const map = { 1: '待确认', 2: '待支付', 3: '已支付', 4: '进行中', 5: '已完成', 6: '已取消', 7: '退款中', 8: '已退款', 9: '争议中' }
  return map[s] || '未知'
}

function statusDesc(s) {
  const map = {
    1: '等待家教接单', 2: '请尽快完成支付', 3: '已支付，等待家教开始上课',
    4: '课程进行中', 5: '课程已完成', 6: '订单已取消',
    7: '退款处理中', 8: '退款已完成', 9: '存在争议，请等待处理'
  }
  return map[s] || ''
}

function modeText(m) {
  return { 1: '线下', 2: '线上', 3: '均可' }[m] || '未知'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function handleAction(type) {
  try {
    if (type === 'accept') {
      await acceptOrder(orderId.value)
      uni.showToast({ title: '接单成功', icon: 'success' })
    } else if (type === 'start') {
      await startOrder(orderId.value)
      uni.showToast({ title: '已开始上课', icon: 'success' })
    } else if (type === 'complete') {
      await completeOrder(orderId.value)
      uni.showToast({ title: '已确认完成', icon: 'success' })
    } else if (type === 'cancel') {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            await cancelOrder(orderId.value, { cancelReason: '用户主动取消', role: userStore.role })
            uni.showToast({ title: '已取消', icon: 'success' })
            loadDetail()
          }
        }
      })
      return
    } else if (type === 'pay') {
      uni.showModal({
        title: '确认支付',
        content: `支付金额: ${(order.value.totalAmount / 100).toFixed(2)}元`,
        success: async (res) => {
          if (res.confirm) {
            try {
              const payData = await prepay(orderId.value)
              // 微信支付
              if (payData && payData.wxPayParams) {
                uni.requestPayment({
                  provider: 'wxpay',
                  timeStamp: payData.wxPayParams.timeStamp,
                  nonceStr: payData.wxPayParams.nonceStr,
                  package: payData.wxPayParams.packageValue,
                  signType: payData.wxPayParams.signType || 'RSA',
                  paySign: payData.wxPayParams.paySign,
                  success: () => {
                    uni.showToast({ title: '支付成功', icon: 'success' })
                    loadDetail()
                  },
                  fail: () => {
                    uni.showToast({ title: '支付取消', icon: 'none' })
                  }
                })
              } else {
                uni.showToast({ title: '支付处理中', icon: 'none' })
                setTimeout(() => loadDetail(), 2000)
              }
            } catch (e) {
              uni.showToast({ title: '支付失败', icon: 'none' })
            }
          }
        }
      })
      return
    }
    loadDetail()
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.page { padding-bottom: 140rpx; background: #f5f5f5; min-height: 100vh; }
.status-bar { padding: 40rpx 30rpx; color: #fff; }
.status-bg-1 { background: linear-gradient(135deg, #FF9800, #F57C00); }
.status-bg-2 { background: linear-gradient(135deg, #4A90D9, #357ABD); }
.status-bg-3 { background: linear-gradient(135deg, #4CAF50, #388E3C); }
.status-bg-4 { background: linear-gradient(135deg, #4CAF50, #388E3C); }
.status-bg-5 { background: linear-gradient(135deg, #999, #777); }
.status-bg-6 { background: linear-gradient(135deg, #F44336, #D32F2F); }
.status-bg-7 { background: linear-gradient(135deg, #FF9800, #F57C00); }
.status-bg-8 { background: linear-gradient(135deg, #999, #777); }
.status-bg-9 { background: linear-gradient(135deg, #F44336, #D32F2F); }
.status-main { font-size: 40rpx; font-weight: bold; display: block; }
.status-sub { font-size: 26rpx; opacity: 0.9; display: block; margin-top: 8rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin: 20rpx; }
.card-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 20rpx; padding-bottom: 16rpx; border-bottom: 1rpx solid #f0f0f0; }
.info-row { display: flex; justify-content: space-between; padding: 10rpx 0; }
.info-label { font-size: 26rpx; color: #999; }
.info-value { font-size: 26rpx; color: #333; }
.info-value.price { color: #E86C3A; font-weight: bold; }
.info-value.desc { color: #666; max-width: 480rpx; text-align: right; }
.info-value.cancel { color: #F44336; }
.log-item { display: flex; align-items: flex-start; padding: 12rpx 0; position: relative; }
.log-dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #4A90D9; margin-top: 8rpx; margin-right: 16rpx; flex-shrink: 0; }
.log-content { flex: 1; display: flex; justify-content: space-between; }
.log-action { font-size: 26rpx; color: #333; }
.log-time { font-size: 22rpx; color: #999; }
.empty-logs { text-align: center; color: #999; font-size: 26rpx; padding: 20rpx; }
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 20rpx 30rpx; display: flex; gap: 16rpx; box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.08); }
.bottom-btn { flex: 1; padding: 20rpx; border-radius: 48rpx; font-size: 30rpx; text-align: center; }
.btn-accept { background: #4A90D9; color: #fff; }
.btn-pay { background: #E86C3A; color: #fff; }
.btn-start { background: #4CAF50; color: #fff; }
.btn-complete { background: #4CAF50; color: #fff; }
.btn-cancel { background: #fff; color: #999; border: 1rpx solid #ddd; }
.loading { text-align: center; padding: 100rpx; color: #999; }
</style>
