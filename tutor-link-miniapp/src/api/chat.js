import { get, put, post } from './request'

// 我的会话列表
export function listConversations() {
  return get('/chat/conversations')
}

// 会话消息历史
export function listMessages(conversationId, limit = 50) {
  return get(`/chat/conversations/${conversationId}/messages`, { limit })
}

// 标记已读
export function markAsRead(conversationId) {
  return put(`/chat/conversations/${conversationId}/read`)
}

// 未读消息数
export function getUnreadCount() {
  return get('/chat/unread-count')
}

// 发送消息
export function sendMessage(receiverId, msgType, content) {
  return post('/chat/send', { receiverId, msgType, content })
}
