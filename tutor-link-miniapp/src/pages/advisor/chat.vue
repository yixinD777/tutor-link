<template>
  <view class="page">
    <!-- 消息列表 -->
    <scroll-view class="msg-list" scroll-y :scroll-into-view="scrollTarget" scroll-with-animation>
      <!-- 欢迎消息 -->
      <view class="msg-item ai" id="msg-welcome">
        <image class="msg-avatar" src="/static/ai-avatar.png" mode="aspectFill" />
        <view class="msg-bubble ai-bubble">
          <text>您好！我是 Tutor-Link 的 AI 教育顾问，可以帮您：\n\n1. 推荐合适的家教老师\n2. 解答平台规则问题\n3. 引导预约试课和下单\n\n请问有什么可以帮您的？</text>
        </view>
      </view>

      <!-- 推荐问题 -->
      <view v-if="showSuggestions && suggestions.length > 0" class="suggestions">
        <view class="suggestion-chip" v-for="(s, i) in suggestions" :key="i" @tap="sendSuggestion(s)">
          <text>{{ s }}</text>
        </view>
      </view>

      <!-- 消息气泡 -->
      <view v-for="(msg, i) in messages" :key="i" :id="'msg-' + i"
            class="msg-item" :class="msg.role">
        <image v-if="msg.role === 'ai'" class="msg-avatar" src="/static/ai-avatar.png" mode="aspectFill" />
        <view class="msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
          <text user-select>{{ msg.content }}</text>
        </view>
        <image v-if="msg.role === 'user'" class="msg-avatar" :src="userAvatar" mode="aspectFill" />
      </view>

      <!-- 加载中 -->
      <view v-if="isLoading" class="msg-item ai">
        <image class="msg-avatar" src="/static/ai-avatar.png" mode="aspectFill" />
        <view class="msg-bubble ai-bubble loading-bubble">
          <view class="typing-indicator">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </view>

      <!-- 底部占位 -->
      <view id="msg-bottom" style="height: 20rpx;"></view>
    </scroll-view>

    <!-- 输入栏 -->
    <view class="input-bar">
      <input class="input-field" v-model="inputText" placeholder="输入您的问题..."
             confirm-type="send" @confirm="sendMessage" :disabled="isLoading" />
      <button class="send-btn" @tap="sendMessage" :disabled="!inputText.trim() || isLoading">
        发送
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chatWithAi, getAiSuggestions } from '../../api/ai'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const messages = ref([])
const inputText = ref('')
const isLoading = ref(false)
const scrollTarget = ref('')
const suggestions = ref([])
const showSuggestions = ref(true)
const userAvatar = ref('/static/default-avatar.png')

onMounted(() => {
  userStore.loadFromStorage()
  if (userStore.avatarUrl) {
    userAvatar.value = userStore.avatarUrl
  }
  loadSuggestions()
})

async function loadSuggestions() {
  try {
    suggestions.value = await getAiSuggestions()
  } catch (e) {
    suggestions.value = [
      '帮我找一个数学家教',
      '退款政策是什么？',
      '如何预约试课？'
    ]
  }
}

function sendSuggestion(text) {
  showSuggestions.value = false
  inputText.value = text
  sendMessage()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  inputText.value = ''
  showSuggestions.value = false

  // Add user message
  messages.value.push({ role: 'user', content: text })
  scrollToBottom()

  // Build history for multi-turn
  const history = messages.value.slice(0, -1).map(m => ({
    role: m.role === 'user' ? 'user' : 'assistant',
    content: m.content
  }))

  isLoading.value = true

  try {
    const res = await chatWithAi(text, null, history)
    messages.value.push({ role: 'ai', content: res.message || '抱歉，暂时无法回答您的问题。' })
  } catch (e) {
    messages.value.push({ role: 'ai', content: '抱歉，服务暂时不可用，请稍后再试。' })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    scrollTarget.value = ''
    setTimeout(() => {
      scrollTarget.value = 'msg-bottom'
    }, 50)
  })
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.msg-list {
  flex: 1;
  padding: 20rpx;
  overflow-y: auto;
}

.msg-item {
  display: flex;
  margin-bottom: 24rpx;
  align-items: flex-start;
}
.msg-item.user {
  justify-content: flex-end;
}
.msg-item.ai {
  justify-content: flex-start;
}

.msg-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.msg-bubble {
  max-width: 70%;
  padding: 20rpx 28rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;
}

.ai-bubble {
  background: #ffffff;
  margin-left: 16rpx;
  border-top-left-radius: 4rpx;
}

.user-bubble {
  background: #4A90D9;
  color: #ffffff;
  margin-right: 16rpx;
  border-top-right-radius: 4rpx;
}

/* 推荐问题 */
.suggestions {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx 0;
  gap: 16rpx;
}
.suggestion-chip {
  background: #ffffff;
  border: 1rpx solid #4A90D9;
  color: #4A90D9;
  border-radius: 32rpx;
  padding: 12rpx 28rpx;
  font-size: 24rpx;
}

/* 加载动画 */
.loading-bubble {
  padding: 24rpx 32rpx;
}
.typing-indicator {
  display: flex;
  gap: 10rpx;
}
.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #999;
  animation: bounce 1.4s infinite ease-in-out;
}
.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入栏 */
.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1rpx solid #e5e5e5;
}

.input-field {
  flex: 1;
  background: #f5f5f5;
  border-radius: 36rpx;
  padding: 16rpx 28rpx;
  font-size: 28rpx;
  margin-right: 16rpx;
}

.send-btn {
  background: #4A90D9;
  color: #ffffff;
  border: none;
  border-radius: 36rpx;
  font-size: 28rpx;
  padding: 16rpx 36rpx;
  line-height: 1.2;
}
.send-btn[disabled] {
  opacity: 0.5;
}
</style>
