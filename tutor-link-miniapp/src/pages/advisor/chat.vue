<template>
  <view class="page">
    <!-- 右上角记忆按钮（H5 导航栏） -->
    <view class="memory-btn" @tap="openMemory">
      <text class="memory-icon">🧠</text>
    </view>

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
      <template v-for="(msg, i) in messages" :key="i">
        <view v-if="msg.role === 'user' || msg.content !== ''"
              :id="'msg-' + i"
              class="msg-item" :class="msg.role">
          <image v-if="msg.role === 'ai'" class="msg-avatar" src="/static/ai-avatar.png" mode="aspectFill" />
          <view class="msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
            <text user-select>{{ msg.content }}</text>
          </view>
          <image v-if="msg.role === 'user'" class="msg-avatar" :src="userAvatar" mode="aspectFill" />
        </view>
      </template>

      <!-- 加载中（等待第一个字出现前显示） -->
      <view v-if="isLoading && messages.length > 0 && messages[messages.length-1].content === ''" class="msg-item ai">
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

    <!-- 记忆抽屉 -->
    <view v-if="showMemoryPopup" class="popup-mask" @tap.self="closeMemory">
      <view class="memory-popup">
        <view class="popup-header">
          <text class="popup-title">AI 已记住的信息</text>
          <text class="popup-close" @tap="closeMemory">✕</text>
        </view>

        <view v-if="memoryLoading" class="memory-loading">
          <text>加载中...</text>
        </view>
        <view v-else-if="!memoryData.summary && (!memoryData.facts || memoryData.facts.length === 0)" class="memory-empty">
          <text>暂无记忆，和 AI 多聊几轮后会自动记录您的偏好</text>
        </view>
        <view v-else class="memory-content">
          <view v-if="memoryData.summary" class="memory-section">
            <text class="section-label">偏好摘要</text>
            <text class="section-text">{{ memoryData.summary }}</text>
          </view>
          <view v-if="memoryData.facts && memoryData.facts.length > 0" class="memory-section">
            <text class="section-label">已知信息</text>
            <view class="fact-list">
              <view class="fact-item" v-for="(f, i) in memoryData.facts" :key="i">
                <text class="fact-dot">·</text>
                <text class="fact-text">{{ f }}</text>
              </view>
            </view>
          </view>
          <view v-if="memoryData.updatedAt" class="memory-updated">
            <text>最后更新：{{ memoryData.updatedAt }}</text>
          </view>
        </view>

        <button class="clear-btn" @tap="onClearMemory" :disabled="memoryLoading">清除记忆</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chatWithAiStream, getAiSuggestions, getAiMemory, clearAiMemory } from '../../api/ai'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const messages = ref([])
const inputText = ref('')
const isLoading = ref(false)
const scrollTarget = ref('')
const suggestions = ref([])
const showSuggestions = ref(true)
const userAvatar = ref('/static/default-avatar.png')
const conversationId = ref(null)
let abortStream = null

// 记忆 popup 状态
const showMemoryPopup = ref(false)
const memoryLoading = ref(false)
const memoryData = ref({ summary: '', facts: [], updatedAt: null })

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

async function openMemory() {
  showMemoryPopup.value = true
  memoryLoading.value = true
  try {
    memoryData.value = await getAiMemory() || { summary: '', facts: [], updatedAt: null }
  } catch (e) {
    memoryData.value = { summary: '', facts: [], updatedAt: null }
  } finally {
    memoryLoading.value = false
  }
}

function closeMemory() {
  showMemoryPopup.value = false
}

async function onClearMemory() {
  uni.showModal({
    title: '确认清除',
    content: '清除后 AI 将不再记得您的偏好信息，确定吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await clearAiMemory()
          memoryData.value = { summary: '', facts: [], updatedAt: null }
          uni.showToast({ title: '记忆已清除', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '清除失败，请重试', icon: 'none' })
        }
      }
    }
  })
}

function sendSuggestion(text) {
  showSuggestions.value = false
  inputText.value = text
  sendMessage()
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  inputText.value = ''
  showSuggestions.value = false

  // Add user message
  messages.value.push({ role: 'user', content: text })
  scrollToBottom()

  isLoading.value = true

  // Add empty AI message placeholder
  const aiMsgIndex = messages.value.length
  messages.value.push({ role: 'ai', content: '' })

  abortStream = chatWithAiStream(
    text,
    conversationId.value,
    // onText: 逐字追加
    (chunk) => {
      messages.value[aiMsgIndex].content += chunk
      scrollToBottom()
    },
    // onDone
    (convId) => {
      if (convId) conversationId.value = convId
      isLoading.value = false
      if (!messages.value[aiMsgIndex].content) {
        messages.value[aiMsgIndex].content = '抱歉，暂时无法回答您的问题。'
      }
      scrollToBottom()
    },
    // onError
    (err) => {
      console.error('AI stream error', err)
      isLoading.value = false
      messages.value[aiMsgIndex].content = '抱歉，服务暂时不可用，请稍后再试。'
      scrollToBottom()
    }
  )
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

/* 记忆按钮（浮在右上角） */
.memory-btn {
  position: fixed;
  top: calc(20rpx + env(safe-area-inset-top));
  right: 24rpx;
  z-index: 100;
  background: rgba(255,255,255,0.9);
  border-radius: 50%;
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.12);
}
.memory-icon {
  font-size: 36rpx;
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

/* 记忆 Popup */
.popup-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.memory-popup {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 40rpx 40rpx calc(40rpx + env(safe-area-inset-bottom));
  max-height: 70vh;
  overflow-y: auto;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}
.popup-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1a1a1a;
}
.popup-close {
  font-size: 36rpx;
  color: #999;
  padding: 8rpx;
}

.memory-loading,
.memory-empty {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  padding: 40rpx 0;
}

.memory-content {
  margin-bottom: 32rpx;
}

.memory-section {
  margin-bottom: 28rpx;
}
.section-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
  display: block;
}
.section-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.fact-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.fact-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}
.fact-dot {
  color: #4A90D9;
  font-size: 32rpx;
  line-height: 1.4;
  flex-shrink: 0;
}
.fact-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.memory-updated {
  margin-top: 24rpx;
  font-size: 22rpx;
  color: #bbb;
}

.clear-btn {
  width: 100%;
  background: #fff;
  border: 1rpx solid #ff4d4f;
  color: #ff4d4f;
  border-radius: 16rpx;
  font-size: 28rpx;
  padding: 22rpx 0;
  margin-top: 8rpx;
}
.clear-btn[disabled] {
  opacity: 0.5;
}
</style>
