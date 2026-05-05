package com.tutorlink.service.ai;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.dao.mapper.AiUserMemoryMapper;
import com.tutorlink.model.entity.AiUserMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 用户跨会话长期记忆服务
 * <p>
 * 存储结构：
 * - Redis Hash（缓存层，TTL=30天）
 *   key   = tutorlink:ai:memory:{userId}
 *   field = "summary" / "facts" / "updatedAt"
 * - MySQL ai_user_memory 表（持久化层，每用户一行 upsert）
 * <p>
 * 读策略：Redis hit → 直接返回；Redis miss → MySQL 回填 Redis
 * 写策略：先写 Redis，再异步写 MySQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMemoryService {

    private static final String KEY_PREFIX = "tutorlink:ai:memory:";
    private static final Duration TTL = Duration.ofDays(30);
    private static final int MAX_FACTS = 20;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AiUserMemoryMapper memoryMapper;

    // ==================== 读 ====================

    public String getSummary(Long userId) {
        if (userId == null) return "";
        try {
            String val = (String) redisTemplate.opsForHash().get(key(userId), "summary");
            if (val != null) return val;
            // Redis miss → 从 MySQL 回填
            return loadFromDbAndCache(userId).map(AiUserMemory::getSummary).orElse("");
        } catch (Exception e) {
            log.warn("Failed to get memory summary for user {}", userId, e);
            return "";
        }
    }

    public List<Map<String, String>> getFacts(Long userId) {
        if (userId == null) return List.of();
        try {
            String val = (String) redisTemplate.opsForHash().get(key(userId), "facts");
            if (val != null) {
                return objectMapper.readValue(val, new TypeReference<>() {});
            }
            // Redis miss → 从 MySQL 回填
            return loadFromDbAndCache(userId)
                    .map(db -> {
                        try {
                            return db.getFacts() != null
                                    ? objectMapper.<List<Map<String, String>>>readValue(db.getFacts(), new TypeReference<>() {})
                                    : new ArrayList<Map<String, String>>();
                        } catch (Exception e) {
                            return new ArrayList<Map<String, String>>();
                        }
                    })
                    .orElse(new ArrayList<>());
        } catch (Exception e) {
            log.warn("Failed to get memory facts for user {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取完整记忆文本（注入 system prompt）
     */
    public String getMemoryForPrompt(Long userId) {
        if (userId == null) return "";
        String summary = getSummary(userId);
        List<Map<String, String>> facts = getFacts(userId);
        if (summary.isBlank() && facts.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        if (!summary.isBlank()) {
            sb.append("### 用户历史偏好摘要\n").append(summary).append("\n\n");
        }
        if (!facts.isEmpty()) {
            sb.append("### 已知关键信息\n");
            facts.forEach(f -> sb.append("- ").append(f.getOrDefault("content", "")).append("\n"));
        }
        return sb.toString();
    }

    /**
     * 获取完整记忆记录（供 API 返回给前端）
     */
    public Map<String, Object> getMemoryRecord(Long userId) {
        if (userId == null) return Map.of();
        String summary = getSummary(userId);
        List<Map<String, String>> facts = getFacts(userId);
        String updatedAt = (String) redisTemplate.opsForHash().get(key(userId), "updatedAt");
        if (updatedAt == null) {
            // 尝试从 MySQL 取
            AiUserMemory db = memoryMapper.selectByUserId(userId);
            if (db != null) {
                updatedAt = db.getUpdateTime() != null
                        ? db.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : null;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("facts", facts.stream().map(f -> f.getOrDefault("content", "")).toList());
        result.put("updatedAt", updatedAt);
        return result;
    }

    // ==================== 写 ====================

    public void updateSummary(Long userId, String summary) {
        if (userId == null || summary == null || summary.isBlank()) return;
        try {
            String k = key(userId);
            redisTemplate.opsForHash().put(k, "summary", summary);
            redisTemplate.opsForHash().put(k, "updatedAt",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            redisTemplate.expire(k, TTL);
            log.debug("Updated memory summary for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to update memory summary for user {}", userId, e);
        }
    }

    public void addFact(Long userId, String content) {
        if (userId == null || content == null || content.isBlank()) return;
        try {
            List<Map<String, String>> facts = new ArrayList<>(getFacts(userId));
            boolean duplicate = facts.stream()
                    .anyMatch(f -> similarity(f.getOrDefault("content", ""), content) > 0.8);
            if (duplicate) return;

            Map<String, String> fact = new LinkedHashMap<>();
            fact.put("content", content);
            fact.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            facts.add(fact);
            if (facts.size() > MAX_FACTS) {
                facts = facts.subList(facts.size() - MAX_FACTS, facts.size());
            }

            String k = key(userId);
            redisTemplate.opsForHash().put(k, "facts", objectMapper.writeValueAsString(facts));
            redisTemplate.expire(k, TTL);
        } catch (Exception e) {
            log.warn("Failed to add fact for user {}", userId, e);
        }
    }

    /**
     * 批量更新（summary + facts），先写 Redis，再异步写 MySQL
     */
    public void updateMemory(Long userId, String summary, List<String> newFacts) {
        updateMemory(userId, summary, newFacts, null);
    }

    public void updateMemory(Long userId, String summary, List<String> newFacts, String sourceConv) {
        if (userId == null) return;
        if (summary != null && !summary.isBlank()) {
            updateSummary(userId, summary);
        }
        if (newFacts != null) {
            newFacts.forEach(f -> addFact(userId, f));
        }
        // 异步持久化到 MySQL
        CompletableFuture.runAsync(() -> persistToDb(userId, sourceConv));
    }

    /**
     * 清除用户记忆（Redis + MySQL 逻辑删除）
     */
    public void clear(Long userId) {
        if (userId == null) return;
        redisTemplate.delete(key(userId));
        try {
            memoryMapper.update(null, new LambdaUpdateWrapper<AiUserMemory>()
                    .eq(AiUserMemory::getUserId, userId)
                    .set(AiUserMemory::getIsDeleted, 1));
            log.debug("Cleared memory for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to clear DB memory for user {}", userId, e);
        }
    }

    // ==================== 内部 ====================

    /**
     * 从 MySQL 加载并回填 Redis
     */
    private Optional<AiUserMemory> loadFromDbAndCache(Long userId) {
        try {
            AiUserMemory db = memoryMapper.selectByUserId(userId);
            if (db == null) return Optional.empty();
            String k = key(userId);
            if (db.getSummary() != null) {
                redisTemplate.opsForHash().put(k, "summary", db.getSummary());
            }
            if (db.getFacts() != null) {
                redisTemplate.opsForHash().put(k, "facts", db.getFacts());
            }
            if (db.getUpdateTime() != null) {
                redisTemplate.opsForHash().put(k, "updatedAt",
                        db.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            redisTemplate.expire(k, TTL);
            log.debug("Restored memory from MySQL for user {}", userId);
            return Optional.of(db);
        } catch (Exception e) {
            log.warn("Failed to load memory from DB for user {}", userId, e);
            return Optional.empty();
        }
    }

    /**
     * 将当前 Redis 中的记忆持久化到 MySQL（upsert）
     */
    private void persistToDb(Long userId, String sourceConv) {
        try {
            String summary = (String) redisTemplate.opsForHash().get(key(userId), "summary");
            String factsJson = (String) redisTemplate.opsForHash().get(key(userId), "facts");

            AiUserMemory existing = memoryMapper.selectByUserId(userId);
            if (existing == null) {
                AiUserMemory record = new AiUserMemory();
                record.setUserId(userId);
                record.setSummary(summary);
                record.setFacts(factsJson);
                record.setSourceConv(sourceConv);
                memoryMapper.insert(record);
            } else {
                memoryMapper.update(null, new LambdaUpdateWrapper<AiUserMemory>()
                        .eq(AiUserMemory::getUserId, userId)
                        .set(summary != null, AiUserMemory::getSummary, summary)
                        .set(factsJson != null, AiUserMemory::getFacts, factsJson)
                        .set(sourceConv != null, AiUserMemory::getSourceConv, sourceConv));
            }
            log.debug("Persisted memory to MySQL for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to persist memory to DB for user {}", userId, e);
        }
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    private double similarity(String a, String b) {
        if (a.isBlank() || b.isBlank()) return 0;
        Set<String> biA = bigrams(a);
        Set<String> biB = bigrams(b);
        Set<String> inter = new HashSet<>(biA);
        inter.retainAll(biB);
        Set<String> union = new HashSet<>(biA);
        union.addAll(biB);
        return union.isEmpty() ? 0 : (double) inter.size() / union.size();
    }

    private Set<String> bigrams(String s) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < s.length() - 1; i++) {
            result.add(s.substring(i, i + 2));
        }
        return result;
    }
}
