package com.tutorlink.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.common.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * AI 会话管理服务
 * 使用 Redis 存储多轮对话历史，支持上下文截断
 * Redis key 包含 userId 命名空间，防止跨用户访问会话数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationService {

    private static final int MAX_ROUNDS = 20;       // 最大保留轮数
    private static final int KEEP_ROUNDS = 16;      // 超出后保留最近轮数
    private static final Duration TTL = Duration.ofHours(2); // 会话 TTL

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 获取会话历史（不含当前消息）
     */
    public List<Map<String, Object>> getHistory(Long userId, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) return new ArrayList<>();
        try {
            String key = RedisKeyUtil.aiConversation(userId, conversationId);
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to load conversation {} for user {}", conversationId, userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 追加一条消息到会话历史
     */
    public void appendMessage(Long userId, String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isBlank()) return;
        try {
            List<Map<String, Object>> history = getHistory(userId, conversationId);
            history.add(Map.of("role", role, "content", content));
            saveHistory(userId, conversationId, history);
        } catch (Exception e) {
            log.warn("Failed to append message to conversation {} for user {}", conversationId, userId, e);
        }
    }

    /**
     * 追加多条消息（含 tool 相关消息）
     */
    public void appendMessages(Long userId, String conversationId, List<Map<String, Object>> messages) {
        if (conversationId == null || conversationId.isBlank()) return;
        try {
            List<Map<String, Object>> history = getHistory(userId, conversationId);
            history.addAll(messages);
            saveHistory(userId, conversationId, history);
        } catch (Exception e) {
            log.warn("Failed to append messages to conversation {} for user {}", conversationId, userId, e);
        }
    }

    /**
     * 保存完整历史（含截断）
     */
    public void saveHistory(Long userId, String conversationId, List<Map<String, Object>> messages) {
        if (conversationId == null || conversationId.isBlank()) return;
        try {
            // 按轮次截断：user+assistant 算一轮
            List<Map<String, Object>> trimmed = trimHistory(messages);
            String key = RedisKeyUtil.aiConversation(userId, conversationId);
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(trimmed), TTL);
        } catch (Exception e) {
            log.warn("Failed to save conversation {} for user {}", conversationId, userId, e);
        }
    }

    /**
     * 刷新 TTL（用户活跃时延长会话）
     */
    public void touch(Long userId, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) return;
        redisTemplate.expire(RedisKeyUtil.aiConversation(userId, conversationId), TTL);
    }

    /**
     * 删除会话
     */
    public void clear(Long userId, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) return;
        redisTemplate.delete(RedisKeyUtil.aiConversation(userId, conversationId));
    }

    /**
     * 超出 MAX_ROUNDS 时保留最近 KEEP_ROUNDS 轮
     * 一轮 = user + assistant（tool 消息跟随 assistant 算在同一轮）
     */
    private List<Map<String, Object>> trimHistory(List<Map<String, Object>> messages) {
        // 统计 user 消息数量作为轮次
        long userCount = messages.stream()
                .filter(m -> "user".equals(m.get("role")))
                .count();

        if (userCount <= MAX_ROUNDS) return messages;

        // 找到从第 (userCount - KEEP_ROUNDS) 个 user 消息开始的位置
        long skip = userCount - KEEP_ROUNDS;
        int startIdx = 0;
        long seen = 0;
        for (int i = 0; i < messages.size(); i++) {
            if ("user".equals(messages.get(i).get("role"))) {
                seen++;
                if (seen > skip) {
                    startIdx = i;
                    break;
                }
            }
        }

        log.info("Conversation trimmed: {} messages -> keeping from index {}", messages.size(), startIdx);
        return new ArrayList<>(messages.subList(startIdx, messages.size()));
    }
}
