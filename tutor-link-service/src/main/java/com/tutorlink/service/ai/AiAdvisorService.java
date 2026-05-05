package com.tutorlink.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.dao.mapper.*;
import com.tutorlink.model.dto.ai.AiChatRequest;
import com.tutorlink.model.dto.ai.AiChatResponse;
import com.tutorlink.model.dto.ai.ToolCallInfo;
import com.tutorlink.model.entity.*;
import com.tutorlink.service.search.TutorSearchService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAdvisorService {

    private final AiAdvisorConfig config;
    private final AiConversationService conversationService;
    private final UserMemoryService userMemoryService;
    private final TutorSearchService tutorSearchService;
    private final TutorProfileMapper tutorProfileMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final SubjectMapper subjectMapper;
    private final TutorSubjectMapper tutorSubjectMapper;
    private final ObjectMapper objectMapper;

    private String platformRules;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("ai/platform-rules.txt");
            platformRules = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info("Platform rules loaded, {} chars", platformRules.length());
        } catch (Exception e) {
            log.warn("Failed to load platform-rules.txt", e);
            platformRules = "平台规则暂未加载";
        }
    }

    /**
     * 非流式聊天
     */
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        validateConfig();

        // 生成或使用已有 conversationId
        String convId = resolveConversationId(request.getConversationId());

        // 从 Redis 加载历史,如果有就用 Redis,否则用前端传入的
        List<Map<String, Object>> messages = buildMessagesWithHistory(request, convId);
        String systemPrompt = buildSystemPrompt(userId);
        List<Map<String, Object>> tools = buildToolDefinitions();

        AiChatResponse response = agenticLoop(userId, messages, systemPrompt, tools, convId);
        response.setConversationId(convId);
        return response;
    }

    /**
     * 流式聊天 (SSE)
     */
    public SseEmitter chatStream(Long userId, AiChatRequest request) {
        validateConfig();

        String convId = resolveConversationId(request.getConversationId());

        SseEmitter emitter = new SseEmitter(120_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> log.warn("SSE error", e));

        CompletableFuture.runAsync(() -> {
            try {
                List<Map<String, Object>> messages = buildMessagesWithHistory(request, convId);
                String systemPrompt = buildSystemPrompt(userId);
                List<Map<String, Object>> tools = buildToolDefinitions();

                streamAgenticLoop(userId, messages, systemPrompt, tools, emitter, convId);

                // 发送 convId 给前端
                emitter.send(SseEmitter.event().data(
                        objectMapper.writeValueAsString(Map.of(
                                "type", "done",
                                "conversationId", convId))));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI chat stream error", e);
                try {
                    emitter.send(SseEmitter.event().data(
                            objectMapper.writeValueAsString(Map.of(
                                    "type", "error",
                                    "message", "AI服务暂时不可用,请稍后再试"))));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 查询用户长期记忆（供前端展示）
     */
    public Map<String, Object> getMemory(Long userId) {
        return userMemoryService.getMemoryRecord(userId);
    }

    /**
     * 清除用户长期记忆
     */
    public void clearMemory(Long userId) {
        userMemoryService.clear(userId);
    }

    /**
     * 推荐问题列表
     */
    public List<String> getSuggestions() {
        return List.of(
                "帮我找一个数学家教",
                "退款政策是什么?",
                "如何预约试课?",
                "家教老师怎么认证的?",
                "线上和线下课程有什么区别?",
                "平台怎么收费的?"
        );
    }

    // ==================== Agentic Loop ====================

    private AiChatResponse agenticLoop(Long userId, List<Map<String, Object>> messages,
                                        String systemPrompt, List<Map<String, Object>> tools,
                                        String convId) {
        List<ToolCallInfo> allToolCalls = new ArrayList<>();
        StringBuilder fullResponse = new StringBuilder();
        // 记录本轮新增的消息,用于持久化
        List<Map<String, Object>> newMessages = new ArrayList<>();

        // 先把用户这条消息加到 newMessages
        newMessages.add(messages.get(messages.size() - 1));

        for (int round = 0; round < 5; round++) {
            Map<String, Object> body = buildClaudeRequest(systemPrompt, messages, tools, false);
            JsonNode responseNode = callClaudeApi(body);

            JsonNode content = responseNode.path("content");
            boolean hasToolUse = false;

            for (JsonNode block : content) {
                String type = block.path("type").asText();
                if ("text".equals(type)) {
                    fullResponse.append(block.path("text").asText());
                } else if ("tool_use".equals(type)) {
                    hasToolUse = true;
                    String toolId = block.path("id").asText();
                    String toolName = block.path("name").asText();
                    JsonNode inputNode = block.path("input");
                    Map<String, Object> input = objectMapper.convertValue(inputNode,
                            new TypeReference<>() {});

                    String result = executeTool(toolName, input, userId);
                    allToolCalls.add(ToolCallInfo.builder()
                            .toolName(toolName)
                            .input(input)
                            .resultSummary(truncate(result, 200))
                            .build());

                    Map<String, Object> assistantMsg = Map.of("role", "assistant", "content", content);
                    Map<String, Object> toolResultMsg = Map.of("role", "user", "content",
                            List.of(Map.of(
                                    "type", "tool_result",
                                    "tool_use_id", toolId,
                                    "content", result)));
                    messages.add(assistantMsg);
                    messages.add(toolResultMsg);
                    newMessages.add(assistantMsg);
                    newMessages.add(toolResultMsg);
                }
            }

            if (!hasToolUse) {
                // 最终 AI 回复存入 Redis
                String finalText = fullResponse.toString();
                newMessages.add(Map.of("role", "assistant", "content", finalText));
                conversationService.appendMessages(convId, newMessages);
                // 异步提炼并更新用户长期记忆（传入 convId 用于溯源）
                extractAndSaveMemoryAsync(userId, newMessages, convId);
                break;
            }
        }

        return AiChatResponse.builder()
                .message(fullResponse.toString())
                .toolCalls(allToolCalls.isEmpty() ? null : allToolCalls)
                .build();
    }

    private void streamAgenticLoop(Long userId, List<Map<String, Object>> messages,
                                    String systemPrompt, List<Map<String, Object>> tools,
                                    SseEmitter emitter, String convId) throws Exception {
        List<Map<String, Object>> newMessages = new ArrayList<>();
        newMessages.add(messages.get(messages.size() - 1)); // user 消息
        StringBuilder fullText = new StringBuilder();

        for (int round = 0; round < 5; round++) {
            Map<String, Object> body = buildClaudeRequest(systemPrompt, messages, tools, true);
            List<JsonNode> contentBlocks = streamClaudeApi(body, emitter);

            boolean hasToolUse = false;
            List<Map<String, Object>> assistantContent = new ArrayList<>();

            for (JsonNode block : contentBlocks) {
                String type = block.path("type").asText();
                if ("text".equals(type)) {
                    fullText.append(block.path("text").asText());
                } else if ("tool_use".equals(type)) {
                    hasToolUse = true;
                    String toolId = block.path("id").asText();
                    String toolName = block.path("name").asText();
                    JsonNode inputNode = block.path("input");
                    Map<String, Object> input = objectMapper.convertValue(inputNode,
                            new TypeReference<>() {});

                    emitter.send(SseEmitter.event().data(
                            objectMapper.writeValueAsString(Map.of(
                                    "type", "tool_call",
                                    "tool", toolName,
                                    "summary", getToolSummary(toolName, input)))));

                    String result = executeTool(toolName, input, userId);

                    assistantContent.add(Map.of(
                            "type", "tool_use",
                            "id", toolId,
                            "name", toolName,
                            "input", input));
                }
            }

            if (!hasToolUse) {
                // 流式结束,存入 Redis
                newMessages.add(Map.of("role", "assistant", "content", fullText.toString()));
                conversationService.appendMessages(convId, newMessages);
                // 异步提炼用户记忆（传入 convId 用于溯源）
                extractAndSaveMemoryAsync(userId, newMessages, convId);
                break;
            }

            Map<String, Object> assistantMsg = Map.of("role", "assistant", "content", assistantContent);
            messages.add(assistantMsg);
            newMessages.add(assistantMsg);

            List<Map<String, Object>> toolResults = contentBlocks.stream()
                    .filter(b -> "tool_use".equals(b.path("type").asText()))
                    .map(b -> {
                        String toolId = b.path("id").asText();
                        String toolName = b.path("name").asText();
                        JsonNode inputNode = b.path("input");
                        Map<String, Object> input = objectMapper.convertValue(inputNode,
                                new TypeReference<>() {});
                        String result = executeTool(toolName, input, userId);
                        Map<String, Object> toolResult = new LinkedHashMap<>();
                        toolResult.put("type", "tool_result");
                        toolResult.put("tool_use_id", toolId);
                        toolResult.put("content", result);
                        return toolResult;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> toolResultMsg = new LinkedHashMap<>();
            toolResultMsg.put("role", "user");
            toolResultMsg.put("content", toolResults);
            messages.add(toolResultMsg);
            newMessages.add(toolResultMsg);
        }
    }

    // ==================== Claude / Anthropic API ====================

    private Map<String, Object> buildClaudeRequest(String systemPrompt,
                                                    List<Map<String, Object>> messages,
                                                    List<Map<String, Object>> tools,
                                                    boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("max_tokens", config.getMaxTokens());
        body.put("system", systemPrompt);
        body.put("messages", messages);
        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
        }
        body.put("stream", stream);
        return body;
    }

    private JsonNode callClaudeApi(Map<String, Object> body) {
        HttpHeaders headers = buildHeaders();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "请求构建失败");
        }

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            RestTemplate rt = config.claudeRestTemplate();
            ResponseEntity<String> response = rt.exchange(
                    config.getBaseUrl() + "/v1/messages",
                    HttpMethod.POST,
                    entity,
                    String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Claude API call failed", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI服务调用失败");
        }
    }

    /**
     * 流式调用 Anthropic API,逐行解析 SSE 事件,返回完整的 content blocks
     */
    private List<JsonNode> streamClaudeApi(Map<String, Object> body, SseEmitter emitter)
            throws Exception {
        HttpHeaders headers = buildHeaders();
        String jsonBody = objectMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate rt = config.claudeRestTemplate();

        return rt.execute(
                config.getBaseUrl() + "/v1/messages",
                HttpMethod.POST,
                request -> {
                    request.getHeaders().putAll(headers);
                    request.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));
                },
                response -> {
                    List<JsonNode> contentBlocks = new ArrayList<>();
                    Map<String, Object> currentToolUse = null;
                    StringBuilder currentText = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data: ")) continue;
                            String data = line.substring(6).trim();
                            if ("[DONE]".equals(data)) break;

                            JsonNode event;
                            try {
                                event = objectMapper.readTree(data);
                            } catch (Exception e) {
                                continue;
                            }
                            String eventType = event.path("type").asText();

                            switch (eventType) {
                                case "content_block_start" -> {
                                    JsonNode block = event.path("content_block");
                                    String blockType = block.path("type").asText();
                                    if ("tool_use".equals(blockType)) {
                                        currentToolUse = new LinkedHashMap<>();
                                        currentToolUse.put("type", "tool_use");
                                        currentToolUse.put("id", block.path("id").asText());
                                        currentToolUse.put("name", block.path("name").asText());
                                        currentToolUse.put("inputJson", new StringBuilder());
                                    } else if ("text".equals(blockType)) {
                                        currentText.setLength(0);
                                    }
                                }
                                case "content_block_delta" -> {
                                    JsonNode delta = event.path("delta");
                                    String deltaType = delta.path("type").asText();
                                    if ("text_delta".equals(deltaType)) {
                                        String text = delta.path("text").asText();
                                        currentText.append(text);
                                        emitter.send(SseEmitter.event().data(
                                                objectMapper.writeValueAsString(Map.of(
                                                        "type", "text",
                                                        "content", text))));
                                    } else if ("input_json_delta".equals(deltaType)) {
                                        if (currentToolUse != null) {
                                            ((StringBuilder) currentToolUse.get("inputJson"))
                                                    .append(delta.path("partial_json").asText());
                                        }
                                    }
                                }
                                case "content_block_stop" -> {
                                    if (currentToolUse != null) {
                                        String inputJsonStr = currentToolUse.get("inputJson").toString();
                                        Map<String, Object> finalToolUse = new LinkedHashMap<>();
                                        finalToolUse.put("type", "tool_use");
                                        finalToolUse.put("id", currentToolUse.get("id"));
                                        finalToolUse.put("name", currentToolUse.get("name"));
                                        try {
                                            finalToolUse.put("input", objectMapper.readValue(
                                                    inputJsonStr.isBlank() ? "{}" : inputJsonStr,
                                                    new TypeReference<Map<String, Object>>() {}));
                                        } catch (Exception e) {
                                            finalToolUse.put("input", new LinkedHashMap<>());
                                        }
                                        contentBlocks.add(objectMapper.valueToTree(finalToolUse));
                                        currentToolUse = null;
                                    } else if (currentText.length() > 0) {
                                        Map<String, Object> textBlock = new LinkedHashMap<>();
                                        textBlock.put("type", "text");
                                        textBlock.put("text", currentText.toString());
                                        contentBlocks.add(objectMapper.valueToTree(textBlock));
                                        currentText.setLength(0);
                                    }
                                }
                                case "error" -> {
                                    String errorMsg = event.path("error").path("message").asText();
                                    log.error("Anthropic stream error: {}", errorMsg);
                                    throw new BusinessException(ResultCode.AI_SERVICE_ERROR, errorMsg);
                                }
                            }
                        }
                    }
                    return contentBlocks;
                });
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", config.getApiKey());
        return headers;
    }

    // ==================== System Prompt & Tools ====================

    private String buildSystemPrompt(Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Role: 首席教育匹配顾问与平台智能客服\n\n");
        sb.append("你是 Tutor-Link 家教直连平台的高级教育顾问。你的任务是为家长推荐最合适的家教老师,并解答关于平台规则的任何问题。\n\n");

        sb.append("## 核心目标\n");
        sb.append("1. **精准推荐**:分析学生情况,匹配最合适的老师并给出推荐理由\n");
        sb.append("2. **温情客服**:解答平台规则疑问,安抚家长的教育焦虑\n");
        sb.append("3. **促成履约**:引导家长完成发布需求、确认试课、安排排期的闭环\n\n");

        sb.append("## 语气要求\n");
        sb.append("- 专业、耐心、共情,像一位经验丰富的老教师\n");
        sb.append("- 评价学生时客观且有建设性,避免负面词汇\n");
        sb.append("- 用\"有很大提升空间\"替代\"差\",用\"遇到了一些挑战\"替代\"糟糕\"\n");
        sb.append("- 每次回答末尾主动推进一步流程\n\n");

        sb.append("## 边界控制\n");
        sb.append("- 只回答教育匹配和平台规则相关的问题\n");
        sb.append("- 遇到知识库中没有的极端问题,引导转接人工客服\n");
        sb.append("- 绝不自行编造规则\n\n");

        // 注入用户履历记忆
        String memory = userMemoryService.getMemoryForPrompt(userId);
        if (!memory.isBlank()) {
            sb.append("## 用户历史记忆(历史对话提炼)\n");
            sb.append(memory);
            sb.append("\n");
        }

        // 注入当前用户信息
        sb.append("## 当前用户信息\n");
        sb.append(getUserContext(userId));
        sb.append("\n");

        // 注入平台规则
        sb.append("## 平台规则知识库\n");
        sb.append(platformRules);
        sb.append("\n");

        return sb.toString();
    }

    private String getUserContext(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) return "用户信息:未知";

            StringBuilder sb = new StringBuilder();
            sb.append("- 角色:").append(user.getRole() == 1 ? "家长" : "家教老师").append("\n");
            sb.append("- 昵称:").append(user.getNickname()).append("\n");

            UserProfile profile = userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
            if (profile != null && profile.getCity() != null) {
                sb.append("- 所在地区:").append(profile.getCity());
                if (profile.getDistrict() != null) {
                    sb.append(" ").append(profile.getDistrict());
                }
                sb.append("\n");
            }

            // Recent order count
            Long orderCount = orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>().eq(Order::getParentUserId, userId));
            sb.append("- 历史订单数:").append(orderCount).append("\n");

            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to get user context for {}", userId, e);
            return "用户信息:获取失败\n";
        }
    }

    private List<Map<String, Object>> buildToolDefinitions() {
        return List.of(
                Map.of(
                        "name", "get_student_record",
                        "description", "查询当前用户的学生学习档案,包括历史订单、学习科目、年级、上课偏好等信息。用于了解学生情况后推荐合适的家教。",
                        "input_schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "student_id", Map.of(
                                                "type", "string",
                                                "description", "学生ID,通常就是当前用户的ID")),
                                "required", List.of("student_id"))),
                Map.of(
                        "name", "search_tutors",
                        "description", "搜索匹配条件的家教老师。根据科目、年级、地区、价格等条件搜索已认证的家教老师列表。",
                        "input_schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "subject", Map.of(
                                                "type", "string",
                                                "description", "科目名称,如:数学、英语、物理"),
                                        "grade", Map.of(
                                                "type", "string",
                                                "description", "年级,如:高一、初三、小学三年级"),
                                        "city", Map.of(
                                                "type", "string",
                                                "description", "城市,如:北京、上海"),
                                        "max_hourly_rate", Map.of(
                                                "type", "integer",
                                                "description", "最高时薪(单位:分),如20000表示200元/时"),
                                        "sort_by", Map.of(
                                                "type", "string",
                                                "enum", List.of("rating", "price_asc", "price_desc"),
                                                "description", "排序方式:rating=评分最高,price_asc=价格最低,price_desc=价格最高")))),
                Map.of(
                        "name", "query_platform_rules",
                        "description", "查询平台规则知识库。当家长询问退款政策、试课规则、认证要求、收费标准、预约流程等问题时使用。",
                        "input_schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "query", Map.of(
                                                "type", "string",
                                                "description", "要查询的规则关键词,如:退款、试课、认证、收费")),
                                "required", List.of("query"))));
    }

    // ==================== Tool Execution ====================

    private String executeTool(String toolName, Map<String, Object> input, Long userId) {
        try {
            return switch (toolName) {
                case "get_student_record" -> handleGetStudentRecord(userId);
                case "search_tutors" -> handleSearchTutors(input);
                case "query_platform_rules" -> handleQueryPlatformRules(input);
                default -> "{\"error\": \"未知工具: " + toolName + "\"}";
            };
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return "{\"error\": \"工具执行失败: " + e.getMessage() + "\"}";
        }
    }

    private String handleGetStudentRecord(Long userId) {
        try {
            // Get user profile
            UserProfile profile = userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));

            // Get order history
            List<Order> orders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getParentUserId, userId)
                            .orderByDesc(Order::getCreateTime)
                            .last("LIMIT 10"));

            // Get reviews written by user
            List<Review> reviews = reviewMapper.selectList(
                    new LambdaQueryWrapper<Review>()
                            .eq(Review::getReviewerId, userId)
                            .last("LIMIT 5"));

            // Build subject name map
            Map<Long, String> subjectNames = new HashMap<>();
            if (!orders.isEmpty()) {
                Set<Long> subjectIds = orders.stream()
                        .map(Order::getSubjectId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!subjectIds.isEmpty()) {
                    subjectMapper.selectBatchIds(subjectIds).forEach(
                            s -> subjectNames.put(s.getId(), s.getName()));
                }
            }

            Map<String, Object> record = new LinkedHashMap<>();
            if (profile != null) {
                record.put("location", buildLocation(profile));
            }
            record.put("order_count", orders.size());

            // Extract grades and subjects
            Set<String> grades = orders.stream()
                    .map(Order::getGrade)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            record.put("grades", grades);

            Set<String> subjects = orders.stream()
                    .map(o -> subjectNames.getOrDefault(o.getSubjectId(), "未知"))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            record.put("subjects", subjects);

            // Teaching mode preference
            long offlineCount = orders.stream()
                    .filter(o -> o.getTeachingMode() != null && o.getTeachingMode() == 1).count();
            long onlineCount = orders.stream()
                    .filter(o -> o.getTeachingMode() != null && o.getTeachingMode() == 2).count();
            record.put("preferred_teaching_mode", offlineCount >= onlineCount ? "线下" : "线上");

            // Average rating given
            if (!reviews.isEmpty()) {
                double avgRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average().orElse(0);
                record.put("average_rating_given", Math.round(avgRating * 10) / 10.0);
            }

            // Recent orders summary
            List<Map<String, Object>> recentOrders = orders.stream().limit(5).map(o -> {
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("subject", subjectNames.getOrDefault(o.getSubjectId(), "未知"));
                summary.put("grade", o.getGrade());
                summary.put("status", o.getStatus());
                summary.put("title", o.getTitle());
                return summary;
            }).toList();
            record.put("recent_orders", recentOrders);

            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"数据序列化失败\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private String handleSearchTutors(Map<String, Object> input) {
        try {
            String subjectName = (String) input.get("subject");
            String city = (String) input.get("city");
            Integer maxRate = input.get("max_hourly_rate") instanceof Number n ? n.intValue() : null;
            String sortBy = (String) input.get("sort_by");

            Long subjectId = null;
            if (subjectName != null) {
                Subject subject = subjectMapper.selectOne(
                        new LambdaQueryWrapper<Subject>().eq(Subject::getName, subjectName));
                if (subject != null) {
                    subjectId = subject.getId();
                }
            }

            IPage<TutorProfile> page = tutorSearchService.searchTutors(
                    subjectId, null, null, maxRate, null, city, null,
                    null, null, null, sortBy, 1, 5);

            List<Map<String, Object>> tutorList = page.getRecords().stream().map(t -> {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("user_id", t.getUserId());
                info.put("university", t.getUniversity());
                info.put("major", t.getMajor());
                info.put("education_level", educationLevelText(t.getEducationLevel()));
                info.put("rating", t.getAvgRating());
                info.put("review_count", t.getRatingCount());
                info.put("order_count", t.getOrderCount());
                info.put("hourly_rate_min", t.getHourlyRateMin());
                info.put("hourly_rate_max", t.getHourlyRateMax());
                info.put("intro", t.getIntro());
                info.put("teaching_style", t.getTeachingStyle());
                info.put("district", t.getDistrict());

                // Get subjects
                List<TutorSubject> subjects = tutorSearchService.getTutorSubjects(t.getUserId());
                List<String> subjectNames = subjects.stream()
                        .map(ts -> {
                            Subject s = subjectMapper.selectById(ts.getSubjectId());
                            return s != null ? s.getName() : "未知";
                        })
                        .toList();
                info.put("subjects", subjectNames);

                return info;
            }).toList();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("count", page.getTotal());
            result.put("tutors", tutorList);

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"搜索失败: " + e.getMessage() + "\"}";
        }
    }

    private String handleQueryPlatformRules(Map<String, Object> input) {
        String query = (String) input.get("query");
        if (query == null || query.isBlank()) {
            return platformRules;
        }

        // Split into sections and score by keyword overlap
        String[] sections = platformRules.split("(?=^## )", -1);
        String queryLower = query.toLowerCase();

        List<String> matched = new ArrayList<>();
        for (String section : sections) {
            String sectionLower = section.toLowerCase();
            if (sectionLower.contains(queryLower)) {
                matched.add(section.strip());
            }
        }

        if (matched.isEmpty()) {
            // Fuzzy match: check each keyword
            String[] keywords = queryLower.split("[\\s,,、]+");
            for (String section : sections) {
                String sectionLower = section.toLowerCase();
                for (String keyword : keywords) {
                    if (keyword.length() >= 2 && sectionLower.contains(keyword)) {
                        matched.add(section.strip());
                        break;
                    }
                }
            }
        }

        if (matched.isEmpty()) {
            return "未找到与\"" + query + "\"相关的平台规则。建议转接人工客服咨询。";
        }

        return String.join("\n\n", matched);
    }

    // ==================== Helpers ====================

    /**
     * 优先从 Redis 加载历史,如果 Redis 没有则用前端传来的 history
     */
    private List<Map<String, Object>> buildMessagesWithHistory(AiChatRequest request, String convId) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // 优先从 Redis 加载
        List<Map<String, Object>> redisHistory = conversationService.getHistory(convId);
        if (!redisHistory.isEmpty()) {
            messages.addAll(redisHistory);
        } else if (request.getHistory() != null) {
            // 降级兼容: Redis 没有时用前端传入的
            for (AiChatRequest.ChatMessage msg : request.getHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        // 加入当前用户消息
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        return messages;
    }

    /**
     * 生成或复用 conversationId
     */
    private String resolveConversationId(String requested) {
        if (requested != null && !requested.isBlank()) {
            conversationService.touch(requested);
            return requested;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 异步提炼对话内容,更新用户长期记忆
     */
    private void extractAndSaveMemoryAsync(Long userId, List<Map<String, Object>> messages, String convId) {
        if (userId == null || messages == null || messages.isEmpty()) return;
        CompletableFuture.runAsync(() -> {
            try {
                log.info("[Memory] Starting AI extraction for user {}, messages={}", userId, messages.size());
                extractAndSaveMemory(userId, messages, convId);
                log.info("[Memory] Extraction completed for user {}", userId);
            } catch (Exception e) {
                log.error("[Memory] Extraction failed for user {}: {}", userId, e.getMessage(), e);
            }
        });
    }

    /**
     * 调用 AI 提炼对话中的用户偏好和关键信息,写入长期记忆
     */
    private void extractAndSaveMemory(Long userId, List<Map<String, Object>> messages, String convId) {
        // 拼接对话文本（只取 user/assistant 的 text content）
        StringBuilder dialogue = new StringBuilder();
        for (Map<String, Object> msg : messages) {
            String role = (String) msg.get("role");
            if (!"user".equals(role) && !"assistant".equals(role)) continue;
            Object content = msg.get("content");
            String text = extractTextFromContent(content);
            if (text != null && !text.isBlank()) {
                dialogue.append(role.equals("user") ? "用户：" : "顾问：")
                        .append(text.trim()).append("\n");
            }
        }
        if (dialogue.isEmpty()) return;

        // 获取已有记忆作为上下文（增量更新）
        String existingMemory = userMemoryService.getMemoryForPrompt(userId);

        String existingSection = existingMemory.isBlank()
                ? "（暂无历史记忆）"
                : existingMemory;

        String prompt = """
                你是一个用户记忆提取助手。请从以下对话中提取用户的关键信息，用于下次对话的个性化服务。

                【已有记忆】
                %s

                【本次对话】
                %s

                请严格输出以下 JSON，不要加 markdown 代码块、不要有多余说明：
                {"summary":"简短摘要，描述用户核心需求和偏好，100字以内","facts":["事实1","事实2"]}

                提取规则：
                - 只记录有实际价值的信息（年级、科目、预算、授课方式偏好、时间偏好、特殊需求等）
                - 已有记忆中已有的内容不要重复输出到 facts
                - 如果本次对话没有新的有价值信息，输出 {"summary":"","facts":[]}
                """.formatted(existingSection, dialogue.toString().trim());

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", config.getModel());
            body.put("max_tokens", 512);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            JsonNode responseNode = callClaudeApi(body);
            String responseText = responseNode.path("content").path(0).path("text").asText("").trim();

            // 清理 markdown 代码块包裹
            if (responseText.startsWith("```")) {
                responseText = responseText.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JsonNode parsed = objectMapper.readTree(responseText);
            String summary = parsed.path("summary").asText("").trim();
            List<String> facts = new ArrayList<>();
            parsed.path("facts").forEach(f -> {
                String fact = f.asText("").trim();
                if (!fact.isBlank()) facts.add(fact);
            });

            if (!summary.isBlank() || !facts.isEmpty()) {
                userMemoryService.updateMemory(userId, summary.isBlank() ? null : summary, facts, convId);
                log.info("[Memory] AI extracted for user {}: summary={}, facts={}", userId, !summary.isBlank(), facts.size());
            } else {
                log.debug("[Memory] No useful info extracted for user {}", userId);
            }
        } catch (Exception e) {
            log.warn("[Memory] AI extraction parse error for user {}: {}", userId, e.getMessage());
        }
    }

    private String extractTextFromContent(Object content) {
        if (content instanceof String s) return s;
        if (content instanceof com.fasterxml.jackson.databind.JsonNode jn) {
            if (jn.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode block : jn) {
                    if ("text".equals(block.path("type").asText())) return block.path("text").asText("");
                }
            } else {
                return jn.asText("");
            }
        }
        return null;
    }

    private String buildLocation(UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        if (profile.getProvince() != null) sb.append(profile.getProvince());
        if (profile.getCity() != null) sb.append(profile.getCity());
        if (profile.getDistrict() != null) sb.append(profile.getDistrict());
        return sb.length() > 0 ? sb.toString() : "未知";
    }

    private String educationLevelText(Integer level) {
        if (level == null) return "未知";
        return switch (level) {
            case 1 -> "本科";
            case 2 -> "硕士";
            case 3 -> "博士";
            default -> "未知";
        };
    }

    private String getToolSummary(String toolName, Map<String, Object> input) {
        return switch (toolName) {
            case "get_student_record" -> "正在查询学生档案...";
            case "search_tutors" -> "正在搜索匹配的家教老师...";
            case "query_platform_rules" -> "正在查询平台规则...";
            default -> "正在处理...";
        };
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    private void validateConfig() {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new BusinessException(ResultCode.AI_NOT_CONFIGURED);
        }
    }
}
