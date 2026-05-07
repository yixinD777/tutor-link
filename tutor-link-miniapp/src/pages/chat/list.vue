<template>
  <view class="page">
    <view class="conv-item" v-for="conv in conversations" :key="conv.id" @tap="goChat(conv)">
      <TlAvatar :src="''" :name="otherUser(conv).nickname || '?'" size="medium" />
      <view class="conv-info">
        <view class="conv-top">
          <text class="conv-name">{{ otherUser(conv).nickname || '用户' }}</text>
          <text class="conv-time">{{ formatSmartTime(conv.lastMessageTime) }}</text>
        </view>
        <view class="conv-bottom">
          <text class="conv-last">{{ conv.lastMessageContent || '' }}</text>
          <view class="conv-badge" v-if="unreadOf(conv) > 0">
            <text class="badge-text">{{ unreadOf(conv) > 99 ? '99+' : unreadOf(conv) }}</text>
          </view>
        </view>
      </view>
    </view>

    <TlEmpty v-if="!loading && conversations.length === 0" icon="💬" text="暂无消息" />
    <TlLoading v-if="loading" text="加载中..." />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { listConversations } from '../../api/chat'
import { useUserStore } from '../../store/user'
import { formatSmartTime } from '../../utils/formatters'

const userStore = useUserStore()
const conversations = ref([])
const loading = ref(false)

onShow(() => {
  if (!userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loadConversations()
})

async function loadConversations() {
  loading.value = true
  try {
    conversations.value = await listConversations()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function otherUser(conv) {
  const isUserA = conv.userAId === userStore.userId
  return {
    userId: isUserA ? conv.userBId : conv.userAId,
    nickname: isUserA ? `用户${conv.userBId}` : `用户${conv.userAId}`
  }
}

function unreadOf(conv) {
  if (conv.userAId === userStore.userId) return conv.userAUnread || 0
  return conv.userBUnread || 0
}

function goChat(conv) {
  const other = otherUser(conv)
  uni.navigateTo({
    url: `/pages/chat/detail?conversationId=${conv.id}&otherUserId=${other.userId}`
  })
}
</script>

<style lang="scss" scoped>
.page {
  padding: 0;
  background: $color-bg-page;
  min-height: 100vh;
}

.conv-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg $spacing-xl;
  background: $color-bg-card;
  border-bottom: 1rpx solid $color-border;
}

.conv-info {
  flex: 1;
  min-width: 0;
  margin-left: $spacing-md;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}

.conv-name {
  font-size: $font-size-md;
  color: $color-text-primary;
  font-weight: $font-weight-bold;
}

.conv-time {
  font-size: $font-size-xs;
  color: $color-text-secondary;
}

.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conv-last {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conv-badge {
  background: $color-danger;
  border-radius: $radius-pill;
  min-width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: $spacing-sm;
}

.badge-text {
  color: #fff;
  font-size: $font-size-xs;
}
</style>
