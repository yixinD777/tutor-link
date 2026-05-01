<template>
  <view class="page">
    <view class="conv-item" v-for="conv in conversations" :key="conv.id" @tap="goChat(conv)">
      <view class="conv-avatar">
        <text class="avatar-text">{{ otherUser(conv).nickname?.[0] || '?' }}</text>
      </view>
      <view class="conv-info">
        <view class="conv-top">
          <text class="conv-name">{{ otherUser(conv).nickname || '用户' }}</text>
          <text class="conv-time">{{ formatTime(conv.lastMessageTime) }}</text>
        </view>
        <view class="conv-bottom">
          <text class="conv-last">{{ conv.lastMessageContent || '' }}</text>
          <view class="conv-badge" v-if="unreadOf(conv) > 0">
            <text class="badge-text">{{ unreadOf(conv) > 99 ? '99+' : unreadOf(conv) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!loading && conversations.length === 0" class="empty">
      <text>暂无消息</text>
    </view>
    <view v-if="loading" class="loading"><text>加载中...</text></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { listConversations } from '../../api/chat'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const conversations = ref([])
const loading = ref(false)

onShow(() => { loadConversations() })

async function loadConversations() {
  loading.value = true
  try {
    conversations.value = await listConversations()
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function otherUser(conv) {
  // 简单展示: 根据当前用户ID判断对方
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

function formatTime(t) {
  if (!t) return ''
  const d = t.replace('T', ' ')
  const now = new Date()
  const date = new Date(d)
  if (now.toDateString() === date.toDateString()) return d.substring(11, 16)
  return d.substring(5, 10)
}

function goChat(conv) {
  const other = otherUser(conv)
  uni.navigateTo({
    url: `/pages/chat/detail?conversationId=${conv.id}&otherUserId=${other.userId}`
  })
}
</script>

<style scoped>
.page { padding: 0; background: #f5f5f5; min-height: 100vh; }
.conv-item { display: flex; align-items: center; padding: 24rpx 30rpx; background: #fff; border-bottom: 1rpx solid #f0f0f0; }
.conv-avatar { width: 88rpx; height: 88rpx; border-radius: 50%; background: #4A90D9; display: flex; align-items: center; justify-content: center; margin-right: 20rpx; flex-shrink: 0; }
.avatar-text { color: #fff; font-size: 36rpx; font-weight: bold; }
.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.conv-name { font-size: 30rpx; color: #333; font-weight: bold; }
.conv-time { font-size: 22rpx; color: #999; }
.conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-last { font-size: 26rpx; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-badge { background: #F44336; border-radius: 24rpx; min-width: 36rpx; height: 36rpx; display: flex; align-items: center; justify-content: center; margin-left: 12rpx; }
.badge-text { color: #fff; font-size: 22rpx; }
.empty { text-align: center; padding: 120rpx; color: #999; font-size: 28rpx; }
.loading { text-align: center; padding: 20rpx; color: #999; font-size: 24rpx; }
</style>
