<template>
  <view class="page">
    <!-- 消息列表 -->
    <scroll-view scroll-y class="msg-list" :scroll-top="scrollTop" :scroll-with-animation="true">
      <view v-for="msg in messages" :key="msg.id"
        class="msg-item" :class="{ 'msg-self': msg.senderId == userId }">

        <!-- 头像 -->
        <image class="msg-avatar" :src="msg.senderId == userId ? userAvatar : otherAvatar" mode="aspectFill" />

        <!-- 气泡 + 时间 -->
        <view class="msg-body">
          <!-- 文本消息 (msgType=1) -->
          <view v-if="msg.msgType === 1" class="msg-bubble">
            <text class="msg-text">{{ msg.content }}</text>
          </view>

          <!-- 订单卡片 (msgType=3) -->
          <view v-else-if="msg.msgType === 3" class="msg-bubble order-card" @tap="goOrderDetail(msg.content)">
            <text class="card-title">📋 家教需求</text>
            <text class="card-info">{{ parseOrderCard(msg.content) }}</text>
            <text class="card-link">点击查看详情 ›</text>
          </view>

          <!-- 试课卡片 (msgType=4) -->
          <view v-else-if="msg.msgType === 4" class="msg-bubble trial-card" @tap="goTrialDetail(msg.content)">
            <text class="card-title">🎓 试课邀请</text>
            <text class="card-info">{{ parseTrialCard(msg.content) }}</text>
            <text class="card-link">点击查看详情 ›</text>
          </view>

          <!-- 排期卡片 (msgType=5) -->
          <view v-else-if="msg.msgType === 5" class="msg-bubble schedule-card" @tap="goScheduleDetail(msg.content)">
            <text class="card-title">📅 课程排期</text>
            <text class="card-info">{{ parseScheduleCard(msg.content) }}</text>
            <text class="card-link">点击查看详情 ›</text>
          </view>

          <!-- 其他消息 -->
          <view v-else class="msg-bubble">
            <text class="msg-text">{{ msg.content }}</text>
          </view>

          <text class="msg-time">{{ formatMsgTime(msg.createTime) }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 输入栏 -->
    <view class="input-bar">
      <input class="msg-input" v-model="inputText" placeholder="输入消息..." confirm-type="send" @confirm="sendTextMsg" />
      <button class="send-btn" @tap="sendTextMsg" :disabled="!inputText.trim()">发送</button>
      <button v-if="isParent && orderId" class="trial-btn" @tap="goCreateTrial">试课</button>
      <button v-if="isParent && orderId" class="schedule-btn" @tap="goCreateSchedule">排期</button>
      <button v-if="isParent && !orderId" class="order-btn" @tap="goCreateOrder">发需求</button>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { listMessages, markAsRead, sendMessage as sendChatMsg } from '../../api/chat'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const userId = userStore.userId
const isParent = userStore.isParent()
const userAvatar = userStore.avatarUrl || '/static/default-avatar.png'
const otherAvatar = ref('/static/default-avatar.png')
const conversationId = ref(null)
const otherUserId = ref(null)
const orderId = ref(null)
const messages = ref([])
const inputText = ref('')
const scrollTop = ref(0)

onLoad((options) => {
  otherUserId.value = Number(options.otherUserId)
  if (options.conversationId) conversationId.value = Number(options.conversationId)
  if (options.orderId) orderId.value = Number(options.orderId)
  if (conversationId.value) loadMessages()
})

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

function parseOrderCard(content) {
  try { const o = JSON.parse(content); return `${o.title || ''}\n${o.grade || ''} · ${o.hourlyRate ? (o.hourlyRate / 100) + '元/时' : ''}` } catch (e) { return content }
}

function parseTrialCard(content) {
  try {
    const o = JSON.parse(content)
    const date = o.trialDate ? o.trialDate.replace('T', ' ').substring(0, 16) : ''
    return `时间: ${date}\n价格: ${o.trialPrice ? (o.trialPrice / 100) + '元' : ''}\n方式: ${o.trialMode === 1 ? '线下' : '线上'}`
  } catch (e) { return content }
}

function parseScheduleCard(content) {
  try {
    const o = JSON.parse(content)
    const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return `${days[o.dayOfWeek] || ''} ${o.startTime || ''}-${o.endTime || ''}\n课时费: ${o.hourlyRate ? (o.hourlyRate / 100) + '元/时' : ''}`
  } catch (e) { return content }
}

function scrollToBottom() { nextTick(() => { scrollTop.value = messages.value.length * 200 }) }
function formatMsgTime(t) { return t ? t.replace('T', ' ').substring(11, 16) : '' }
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #f5f5f5; }
.msg-list { flex: 1; padding: 20rpx; }

/* 消息行：横向，对方消息头像在左，自己消息头像在右 */
.msg-item { display: flex; flex-direction: row; margin-bottom: 24rpx; align-items: flex-start; }
.msg-self { flex-direction: row-reverse; }

.msg-avatar { width: 72rpx; height: 72rpx; border-radius: 50%; flex-shrink: 0; }

/* 气泡+时间竖向排列 */
.msg-body { display: flex; flex-direction: column; max-width: 70%; margin: 0 16rpx; }
.msg-self .msg-body { align-items: flex-end; }

.msg-bubble { padding: 20rpx 24rpx; border-radius: 16rpx; background: #fff; }
.msg-self .msg-bubble { background: #4A90D9; border-top-right-radius: 4rpx; }
.msg-item:not(.msg-self) .msg-bubble { border-top-left-radius: 4rpx; }
.msg-text { font-size: 28rpx; line-height: 1.5; color: #333; word-break: break-all; }
.msg-self .msg-text { color: #fff; }
.msg-time { font-size: 20rpx; color: #999; margin-top: 4rpx; }

.order-card { background: #fff; border: 2rpx solid #4A90D9; }
.trial-card { background: #fff; border: 2rpx solid #FF9500; }
.schedule-card { background: #fff; border: 2rpx solid #07C160; }
.msg-self .order-card { background: #EBF3FB; }
.msg-self .trial-card { background: #FFF8F0; }
.msg-self .schedule-card { background: #F0FFF4; }
.card-title { font-size: 28rpx; font-weight: bold; color: #4A90D9; display: block; margin-bottom: 8rpx; }
.trial-card .card-title { color: #FF9500; }
.schedule-card .card-title { color: #07C160; }
.card-info { font-size: 26rpx; color: #333; display: block; white-space: pre-line; }
.card-link { font-size: 24rpx; color: #4A90D9; display: block; margin-top: 8rpx; }

.input-bar { display: flex; align-items: center; padding: 16rpx 20rpx; background: #fff; border-top: 1rpx solid #f0f0f0; flex-wrap: wrap; gap: 8rpx; }
.msg-input { flex: 1; background: #f5f5f5; border-radius: 32rpx; padding: 16rpx 24rpx; font-size: 28rpx; min-width: 200rpx; }
.send-btn { background: #4A90D9; color: #fff; border-radius: 32rpx; font-size: 28rpx; padding: 16rpx 28rpx; line-height: 1; }
.send-btn[disabled] { background: #ccc; }
.order-btn { background: #FF9500; color: #fff; border-radius: 32rpx; font-size: 28rpx; padding: 16rpx 28rpx; line-height: 1; }
.trial-btn { background: #FF9500; color: #fff; border-radius: 32rpx; font-size: 26rpx; padding: 14rpx 24rpx; line-height: 1; }
.schedule-btn { background: #07C160; color: #fff; border-radius: 32rpx; font-size: 26rpx; padding: 14rpx 24rpx; line-height: 1; }
</style>
