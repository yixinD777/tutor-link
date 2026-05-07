<template>
  <view class="page">
    <!-- 消息列表 -->
    <scroll-view scroll-y class="msg-list" :scroll-top="scrollTop" :scroll-with-animation="true">
      <view v-for="msg in messages" :key="msg.id"
        class="msg-item" :class="{ 'msg-self': msg.senderId == userId }">

        <!-- 头像 -->
        <TlAvatar
          :src="msg.senderId == userId ? userAvatar : otherAvatar"
          size="medium"
        />

        <!-- 气泡 + 时间 -->
        <view class="msg-body">
          <!-- 文本消息 (msgType=1) -->
          <TlChatBubble v-if="msg.msgType === 1" :role="msg.senderId == userId ? 'self' : 'other'">
            <text user-select>{{ msg.content }}</text>
          </TlChatBubble>

          <!-- 订单卡片 (msgType=3) -->
          <TlChatBubble v-else-if="msg.msgType === 3" :role="msg.senderId == userId ? 'self' : 'other'">
            <view class="card order-card" @tap="goOrderDetail(msg.content)">
              <view class="card-header">
                <text class="card-emoji">📋</text>
                <text class="card-title">家教需求</text>
                <TlStatusBadge v-if="parseOrderData(msg.content)?.status" :status="parseOrderData(msg.content).status" type="order" />
              </view>
              <text class="card-info">{{ parseOrderCard(msg.content) }}</text>
              <text class="card-link">点击查看详情 ›</text>
            </view>
          </TlChatBubble>

          <!-- 试课卡片 (msgType=4) -->
          <TlChatBubble v-else-if="msg.msgType === 4" :role="msg.senderId == userId ? 'self' : 'other'">
            <view class="card trial-card" @tap="goTrialDetail(msg.content)">
              <view class="card-header">
                <text class="card-emoji">🎓</text>
                <text class="card-title trial-title">试课邀请</text>
                <TlTag v-if="parseTrialData(msg.content)?.trialMode" :text="modeText(parseTrialData(msg.content).trialMode)" type="mode" />
              </view>
              <text class="card-info">{{ parseTrialCard(msg.content) }}</text>
              <text class="card-link">点击查看详情 ›</text>
            </view>
          </TlChatBubble>

          <!-- 排期卡片 (msgType=5) -->
          <TlChatBubble v-else-if="msg.msgType === 5" :role="msg.senderId == userId ? 'self' : 'other'">
            <view class="card schedule-card" @tap="goScheduleDetail(msg.content)">
              <view class="card-header">
                <text class="card-emoji">📅</text>
                <text class="card-title schedule-title">课程排期</text>
              </view>
              <text class="card-info">{{ parseScheduleCard(msg.content) }}</text>
              <text class="card-link">点击查看详情 ›</text>
            </view>
          </TlChatBubble>

          <!-- 其他消息 -->
          <TlChatBubble v-else :role="msg.senderId == userId ? 'self' : 'other'">
            <text user-select>{{ msg.content }}</text>
          </TlChatBubble>

          <text class="msg-time">{{ formatMsgTime(msg.createTime) }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 输入栏 -->
    <TlChatInputBar
      v-model="inputText"
      placeholder="输入消息..."
      @send="sendTextMsg"
    >
      <template #actions>
        <TlButton v-if="isParent && !orderId" type="warning" size="small" @tap="goCreateOrder">发需求</TlButton>
        <TlButton v-if="isParent && orderStatus === 11" type="danger" size="small" @tap="handleConfirmDelegation">确认委托</TlButton>
        <TlButton v-if="isParent && orderStatus >= 2 && orderId" type="warning" size="small" @tap="goCreateTrial">试课</TlButton>
        <TlButton v-if="isParent && orderStatus >= 2 && orderId" type="success" size="small" @tap="goCreateSchedule">排期</TlButton>
        <TlButton v-if="isTutor && orderStatus === 1 && orderId" type="primary" size="small" @tap="handleExpressInterest">感兴趣</TlButton>
      </template>
    </TlChatInputBar>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { listMessages, markAsRead, sendMessage as sendChatMsg } from '../../api/chat'
import { expressInterest, confirmDelegation } from '../../api/order'
import { get } from '../../api/request'
import { useUserStore } from '../../store/user'
import { formatMsgTime, modeText, formatPrice } from '../../utils/formatters'

const userStore = useUserStore()
const userId = userStore.userId
const isParent = userStore.isParent()
const isTutor = userStore.isTutor()
const userAvatar = userStore.avatarUrl || '/static/default-avatar.svg'
const otherAvatar = ref('/static/default-avatar.svg')
const conversationId = ref(null)
const otherUserId = ref(null)
const orderId = ref(null)
const orderStatus = ref(0)
const messages = ref([])
const inputText = ref('')
const scrollTop = ref(0)

onLoad((options) => {
  otherUserId.value = options.otherUserId
  if (options.conversationId) conversationId.value = options.conversationId
  if (options.orderId) orderId.value = options.orderId
  if (options.orderStatus) orderStatus.value = parseInt(options.orderStatus)
  if (conversationId.value) loadMessages()
  if (orderId.value) loadOrderStatus()
  loadOtherUserInfo()
})

async function loadOtherUserInfo() {
  if (!otherUserId.value) return
  try {
    const data = await get(`/users/${otherUserId.value}/basic`)
    if (data?.avatarUrl) otherAvatar.value = data.avatarUrl
  } catch (e) { /* 获取失败时保持默认头像 */ }
}

async function loadMessages() {
  if (!conversationId.value) return
  try {
    const data = await listMessages(conversationId.value, 50)
    messages.value = (data || []).reverse()
    scrollToBottom()
    markAsRead(conversationId.value)
  } catch (e) { console.error(e) }
}

async function sendTextMsg() {
  const text = inputText.value.trim()
  if (!text) return
  await doSend(1, text)
  inputText.value = ''
}

async function doSend(msgType, content) {
  const tempMsg = {
    id: 'temp_' + Date.now(),
    senderId: userId, receiverId: otherUserId.value,
    msgType, content, createTime: new Date().toISOString()
  }
  messages.value.push(tempMsg)
  scrollToBottom()

  try {
    const msg = await sendChatMsg(otherUserId.value, msgType, content)
    const idx = messages.value.findIndex(m => m.id === tempMsg.id)
    if (idx !== -1) messages.value.splice(idx, 1, msg)
    if (!conversationId.value && msg.conversationId) conversationId.value = msg.conversationId
  } catch (e) {
    uni.showToast({ title: '发送失败', icon: 'none' })
  }
}

async function loadOrderStatus() {
  if (!orderId.value) return
  try {
    const data = await get(`/orders/${orderId.value}`)
    if (data) orderStatus.value = data.status
  } catch (e) { /* ignore */ }
}

async function handleExpressInterest() {
  if (!orderId.value) return
  uni.showModal({
    title: '表达意向',
    content: '确定对这个家教需求感兴趣吗？确认后家长将看到您的意向。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await expressInterest(orderId.value)
          orderStatus.value = 11
          uni.showToast({ title: '已表达意向', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: e.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

async function handleConfirmDelegation() {
  if (!orderId.value) return
  uni.showModal({
    title: '确认委托',
    content: '确定委托这位家教吗？确认后对方将正式成为您的授课老师。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await confirmDelegation(orderId.value)
          orderStatus.value = 2
          uni.showToast({ title: '委托成功', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: e.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

function goCreateOrder() {
  uni.navigateTo({ url: `/pages/order/create?tutorUserId=${otherUserId.value}` })
}

function goCreateTrial() {
  uni.navigateTo({ url: `/pages/trial/create?orderId=${orderId.value}&tutorUserId=${otherUserId.value}` })
}

function goCreateSchedule() {
  uni.navigateTo({ url: `/pages/schedule/create?orderId=${orderId.value}` })
}

function goOrderDetail(content) {
  try { const o = JSON.parse(content); if (o.orderId) uni.navigateTo({ url: `/pages/order/detail?id=${o.orderId}` }) } catch (e) {}
}

function goTrialDetail(content) {
  try { const o = JSON.parse(content); if (o.trialId) uni.navigateTo({ url: `/pages/trial/detail?id=${o.trialId}` }) } catch (e) {}
}

function goScheduleDetail(content) {
  try { const o = JSON.parse(content); if (o.scheduleId) uni.navigateTo({ url: `/pages/schedule/detail?scheduleId=${o.scheduleId}` }) } catch (e) {}
}

function parseOrderData(content) {
  try { return JSON.parse(content) } catch (e) { return null }
}

function parseTrialData(content) {
  try { return JSON.parse(content) } catch (e) { return null }
}

function parseOrderCard(content) {
  try {
    const o = JSON.parse(content)
    return `${o.title || ''}\n${o.grade || ''} · ${o.hourlyRate ? formatPrice(o.hourlyRate) + '/时' : ''}`
  } catch (e) { return content }
}

function parseTrialCard(content) {
  try {
    const o = JSON.parse(content)
    const date = o.trialDate ? formatTime(o.trialDate) : ''
    return `时间: ${date}\n价格: ${o.trialPrice ? formatPrice(o.trialPrice) : ''}\n方式: ${modeText(o.trialMode)}`
  } catch (e) { return content }
}

function parseScheduleCard(content) {
  try {
    const o = JSON.parse(content)
    const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return `${days[o.dayOfWeek] || ''} ${o.startTime || ''}-${o.endTime || ''}\n课时费: ${o.hourlyRate ? formatPrice(o.hourlyRate) + '/时' : ''}`
  } catch (e) { return content }
}

function scrollToBottom() { nextTick(() => { scrollTop.value = messages.value.length * 200 }) }
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $color-bg-page;
}

.msg-list {
  flex: 1;
  padding: $spacing-page;
}

/* 消息行：横向，对方消息头像在左，自己消息头像在右 */
.msg-item {
  display: flex;
  flex-direction: row;
  margin-bottom: $spacing-lg;
  align-items: flex-start;
}

.msg-self {
  flex-direction: row-reverse;
}

/* 气泡+时间竖向排列 */
.msg-body {
  display: flex;
  flex-direction: column;
  max-width: 70%;
  margin: 0 $spacing-sm;
}

.msg-self .msg-body {
  align-items: flex-end;
}

.msg-time {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  margin-top: $spacing-xs;
}

/* ---- 富消息卡片 ---- */
.card {
  border-radius: $radius-md;
  padding: $spacing-md;
  overflow: hidden;
}

.order-card {
  border: 2rpx solid $color-primary;

  .card-title {
    color: $color-primary;
  }
}

.trial-card {
  border: 2rpx solid $color-warning;

  .card-title {
    color: $color-warning;
  }

  .trial-title {
    color: $color-warning;
  }
}

.schedule-card {
  border: 2rpx solid $color-success;

  .card-title {
    color: $color-success;
  }

  .schedule-title {
    color: $color-success;
  }
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-sm;
  gap: $spacing-xs;
}

.card-emoji {
  font-size: $font-size-base;
}

.card-title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $color-primary;
  margin-right: $spacing-xs;
}

.card-info {
  font-size: $font-size-sm;
  color: $color-text-regular;
  white-space: pre-line;
}

.card-link {
  font-size: $font-size-sm;
  color: $color-primary;
  margin-top: $spacing-xs;
}
</style>
