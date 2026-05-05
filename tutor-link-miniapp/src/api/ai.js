import { get, post } from './request'

export function chatWithAi(message, conversationId, history) {
  return post('/ai/chat', { message, conversationId, history })
}

export function getAiSuggestions() {
  return get('/ai/suggestions')
}
