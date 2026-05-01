<template>
  <view class="page">
    <!-- 消息列表 -->
    <scroll-view
      scroll-y
      class="msg-list"
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <view
        v-for="msg in messages" :key="msg.id"
        class="msg-item" :class="{ 'msg-self': msg.senderId === userId }"
      >
        <view class="msg-bubble">
          <text class="msg-text">{{ msg.content }}</text>
        </view>
        <text class="msg-time">{{ formatMsgTime(msg.createTime) }}</text>
      </view>
    </scroll-view>

    <!-- 输入栏 -->
    <view class="input-bar">
      <input
        class="msg-input"
        v-model="inputText"
        placeholder="输入消息..."
        confirm-type="send"
        @confirm="sendMessage"
      />
      <button class="send-btn" @tap="sendMessage" :disabled="!inputText.trim()">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { listMessages, markAsRead } from '../../api/chat'
import { useUserStore } from '../../store/user'
import { getToken } from '../../api/request'

const userStore = useUserStore()
const userId = userStore.userId
const conversationId = ref(null)
const otherUserId = ref(null)
const messages = ref([])
const inputText = ref('')
const scrollTop = ref(0)
let socketTask = null

onLoad((options) => {
  conversationId.value = options.conversationId
  otherUserId.value = options.otherUserId
  loadMessages()
  connectWebSocket()
})

onUnload(() => {
  if (socketTask) {
    socketTask.close()
    socketTask = null
  }
})

async function loadMessages() {
  try {
    const data = await listMessages(conversationId.value, 50)
    // 接口返回倒序，翻转成正序显示
    messages.value = (data || []).reverse()
    scrollToBottom()
    markAsRead(conversationId.value)
  } catch (e) { console.error(e) }
}

function connectWebSocket() {
  const token = getToken()
  // 使用 SockJS + STOMP 的 WebSocket 连接
  // uni-app 不原生支持 STOMP，这里使用简易 WebSocket 模式
  // 实际生产环境可引入 stomp.js 库
  const wsUrl = `ws://localhost:8080/ws/chat?token=${token}`
  try {
    socketTask = uni.connectSocket({ url: wsUrl, complete: () => {} })
    uni.onSocketMessage((res) => {
      try {
        const msg = JSON.parse(res.data)
        messages.value.push(msg)
        scrollToBottom()
      } catch (e) { console.error(e) }
    })
    uni.onSocketError(() => {
      console.warn('WebSocket connection error')
    })
    uni.onSocketClose(() => {
      console.log('WebSocket closed')
    })
  } catch (e) {
    console.warn('WebSocket not available, using REST polling')
    startPolling()
  }
}

let pollingTimer = null
function startPolling() {
  pollingTimer = setInterval(async () => {
    try {
      const data = await listMessages(conversationId.value, 1)
      if (data && data.length > 0) {
        const latestMsg = data[0]
        if (!messages.value.find(m => m.id === latestMsg.id)) {
          messages.value.push(latestMsg)
          scrollToBottom()
        }
      }
    } catch (e) { console.error(e) }
  }, 5000)
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return

  // 通过 REST API 发送 (可靠的备选方案)
  // WebSocket STOMP 消息需要 stomp.js 库支持
  // 这里简化为直接使用 HTTP
  const msg = {
    id: Date.now(),
    conversationId: conversationId.value,
    senderId: userId,
    receiverId: otherUserId.value,
    msgType: 1,
    content: text,
    createTime: new Date().toISOString()
  }

  // 乐观更新UI
  messages.value.push(msg)
  inputText.value = ''
  scrollToBottom()

  // 发送 WebSocket 消息
  if (socketTask) {
    uni.sendSocketMessage({
      data: JSON.stringify({
        receiverId: otherUserId.value,
        msgType: 1,
        content: text
      })
    })
  }
}

function scrollToBottom() {
  nextTick(() => {
    scrollTop.value = messages.value.length * 200
  })
}

function formatMsgTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(11, 16)
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; background: #f5f5f5; }
.msg-list { flex: 1; padding: 20rpx; }
.msg-item { display: flex; flex-direction: column; margin-bottom: 24rpx; align-items: flex-start; }
.msg-self { align-items: flex-end; }
.msg-bubble { max-width: 70%; padding: 20rpx 24rpx; border-radius: 16rpx; background: #fff; }
.msg-self .msg-bubble { background: #4A90D9; }
.msg-text { font-size: 28rpx; line-height: 1.5; color: #333; word-break: break-all; }
.msg-self .msg-text { color: #fff; }
.msg-time { font-size: 20rpx; color: #999; margin-top: 4rpx; }
.input-bar { display: flex; align-items: center; padding: 16rpx 20rpx; background: #fff; border-top: 1rpx solid #f0f0f0; }
.msg-input { flex: 1; background: #f5f5f5; border-radius: 32rpx; padding: 16rpx 24rpx; font-size: 28rpx; margin-right: 16rpx; }
.send-btn { background: #4A90D9; color: #fff; border-radius: 32rpx; font-size: 28rpx; padding: 16rpx 32rpx; line-height: 1; }
.send-btn[disabled] { background: #ccc; }
</style>
