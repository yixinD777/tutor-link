<template>
  <view class="page">
    <!-- 右上角记忆按钮（H5 导航栏） -->
    <view class="memory-btn" @tap="openMemory">
      <text class="memory-icon">🧠</text>
    </view>

    <!-- 消息列表 -->
    <scroll-view class="msg-list" scroll-y :scroll-into-view="scrollTarget" scroll-with-animation>
      <!-- 欢迎消息 -->
      <view class="msg-item msg-ai" id="msg-welcome">
        <TlAvatar src="/static/ai-avatar.svg" size="medium" />
        <TlChatBubble role="ai">
          <text>您好！我是 Tutor-Link 的 AI 教育顾问，可以帮您：\n\n1. 推荐合适的家教老师\n2. 解答平台规则问题\n3. 引导预约试课和下单\n\n请问有什么可以帮您的？</text>
        </TlChatBubble>
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
              class="msg-item" :class="msg.role === 'user' ? 'msg-self' : 'msg-ai'">
          <TlAvatar v-if="msg.role === 'ai'" src="/static/ai-avatar.svg" size="medium" />
          <TlChatBubble :role="msg.role === 'user' ? 'self' : 'ai'">
            <text user-select>{{ msg.content }}</text>
          </TlChatBubble>
          <TlAvatar v-if="msg.role === 'user'" :src="userAvatar" size="medium" />
        </view>
      </template>

      <!-- 加载中（等待第一个字出现前显示） -->
      <view v-if="isLoading && messages.length > 0 && messages[messages.length-1].content === ''" class="msg-item msg-ai">
        <TlAvatar src="/static/ai-avatar.svg" size="medium" />
        <TlChatBubble role="ai">
          <TlLoading type="dots" />
        </TlChatBubble>
      </view>

      <!-- 底部占位 -->
      <view id="msg-bottom" style="height: 20rpx;"></view>
    </scroll-view>

    <!-- 输入栏 -->
    <TlChatInputBar
      v-model="inputText"
      placeholder="输入您的问题..."
      :disabled="isLoading"
      @send="sendMessage"
    />

    <!-- 记忆抽屉 -->
    <view v-if="showMemoryPopup" class="popup-mask" @tap.self="closeMemory">
      <view class="memory-popup">
        <view class="popup-header">
          <text class="popup-title">AI 已记住的信息</text>
          <text class="popup-close" @tap="closeMemory">✕</text>
        </view>

        <TlLoading v-if="memoryLoading" type="spinner" text="加载中..." />
        <TlEmpty v-else-if="!memoryData.summary && (!memoryData.facts || memoryData.facts.length === 0)"
          icon="🧠"
          text="暂无记忆，和 AI 多聊几轮后会自动记录您的偏好"
        />
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

        <TlButton type="danger-outline" block size="medium" :disabled="memoryLoading" @tap="onClearMemory">
          清除记忆
        </TlButton>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chatWithAiStream, getAiSuggestions, getAiMemory, clearAiMemory } from '../../api/ai'
import { get } from '../../api/request'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const messages = ref([])
const inputText = ref('')
const isLoading = ref(false)
const scrollTarget = ref('')
const suggestions = ref([])
const showSuggestions = ref(true)
const userAvatar = ref('/static/default-avatar.svg')
const conversationId = ref(null)
let abortStream = null

// 记忆 popup 状态
const showMemoryPopup = ref(false)
const memoryLoading = ref(false)
const memoryData = ref({ summary: '', facts: [], updatedAt: null })

onMounted(async () => {
  userStore.loadFromStorage()
  // 从服务器拉取最新头像，确保登录后修改头像能同步
  if (userStore.isLoggedIn) {
    try {
      const user = await get('/users/me')
      if (user?.avatarUrl) {
        userAvatar.value = user.avatarUrl
        userStore.avatarUrl = user.avatarUrl
        const info = uni.getStorageSync('userInfo')
        if (info) {
          const data = JSON.parse(info)
          data.avatarUrl = user.avatarUrl
          uni.setStorageSync('userInfo', JSON.stringify(data))
        }
      } else if (userStore.avatarUrl) {
        userAvatar.value = userStore.avatarUrl
      }
    } catch (e) {
      if (userStore.avatarUrl) userAvatar.value = userStore.avatarUrl
    }
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

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $color-bg-page;
}

/* 记忆按钮（浮在右上角） */
.memory-btn {
  position: fixed;
  top: calc(#{$spacing-page + 8rpx} + env(safe-area-inset-top));
  right: $spacing-lg;
  z-index: $z-index-fixed;
  background: rgba($color-bg-card, 0.9);
  border-radius: $radius-round;
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  box-shadow: $shadow-popup;
}

.memory-icon {
  font-size: $font-size-lg;
}

.msg-list {
  flex: 1;
  padding: $spacing-page;
  overflow-y: auto;
}

.msg-item {
  display: flex;
  margin-bottom: $spacing-lg;
  align-items: flex-start;
  gap: $spacing-sm;
}

.msg-self {
  justify-content: flex-end;
}

.msg-ai {
  justify-content: flex-start;
}

/* 推荐问题 */
.suggestions {
  display: flex;
  flex-wrap: wrap;
  padding: $spacing-sm 0;
  gap: $spacing-sm;
}

.suggestion-chip {
  background: $color-bg-card;
  border: 2rpx solid $color-primary;
  color: $color-primary;
  border-radius: $radius-pill;
  padding: $spacing-sm $spacing-lg;
  font-size: $font-size-sm;
}

/* 记忆 Popup */
.popup-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: $z-index-popup;
  display: flex;
  align-items: flex-end;
}

.memory-popup {
  width: 100%;
  background: $color-bg-card;
  border-radius: $radius-xl $radius-xl 0 0;
  padding: $spacing-2xl $spacing-2xl calc(#{$spacing-2xl} + env(safe-area-inset-bottom));
  max-height: 70vh;
  overflow-y: auto;
}

.popup-header {
  @include flex-between;
  margin-bottom: $spacing-xl;
}

.popup-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.popup-close {
  font-size: $font-size-lg;
  color: $color-text-secondary;
  padding: $spacing-xs;
}

.memory-content {
  margin-bottom: $spacing-xl;
}

.memory-section {
  margin-bottom: $spacing-lg;
}

.section-label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-bottom: $spacing-sm;
  display: block;
}

.section-text {
  font-size: $font-size-base;
  color: $color-text-regular;
  line-height: $line-height-relaxed;
}

.fact-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.fact-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-sm;
}

.fact-dot {
  color: $color-primary;
  font-size: $font-size-md;
  line-height: $line-height-normal;
  flex-shrink: 0;
}

.fact-text {
  font-size: $font-size-base;
  color: $color-text-regular;
  line-height: $line-height-relaxed;
}

.memory-updated {
  margin-top: $spacing-lg;
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}
</style>
