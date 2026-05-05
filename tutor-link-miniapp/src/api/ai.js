import { get, post, del, getToken } from './request'

export function chatWithAi(message, conversationId, history) {
  return post('/ai/chat', { message, conversationId, history })
}

export function getAiSuggestions() {
  return get('/ai/suggestions')
}

/**
 * 流式 AI 聊天（SSE POST），仅 H5 可用
 * @param {string} message
 * @param {string|null} conversationId
 * @param {Function} onText - 收到文字片段回调 (text: string)
 * @param {Function} onDone - 完成回调 (conversationId: string)
 * @param {Function} onError - 错误回调
 * @returns {Function} abort 函数
 */
export function chatWithAiStream(message, conversationId, onText, onDone, onError) {
  const token = getToken()
  const url = '/api/v1/ai/chat/stream'

  const controller = new AbortController()

  fetch(url, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify({ message, conversationId }),
    signal: controller.signal
  }).then(async res => {
    if (!res.ok) {
      onError && onError(new Error(`HTTP ${res.status}`))
      return
    }

    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop()

      for (const line of lines) {
        if (!line.startsWith('data:')) continue
        const data = line.slice(5).trim()
        if (!data) continue

        try {
          const event = JSON.parse(data)
          if (event.type === 'text') {
            onText && onText(event.content)
          } else if (event.type === 'done') {
            onDone && onDone(event.conversationId || conversationId)
          } else if (event.type === 'error') {
            onError && onError(new Error(event.message))
          }
        } catch (e) {
          // 忽略解析失败
        }
      }
    }

    onDone && onDone(conversationId)
  }).catch(err => {
    if (err.name !== 'AbortError') {
      onError && onError(err)
    }
  })

  return () => controller.abort()
}

export function getAiMemory() {
  return get('/ai/memory')
}

export function clearAiMemory() {
  return del('/ai/memory')
}
