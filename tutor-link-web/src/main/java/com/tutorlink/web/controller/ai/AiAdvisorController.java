package com.tutorlink.web.controller.ai;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.ai.AiChatRequest;
import com.tutorlink.model.dto.ai.AiChatResponse;
import com.tutorlink.service.ai.AiAdvisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Tag(name = "AI顾问")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;

    @Operation(summary = "AI聊天（非流式）")
    @PostMapping("/chat")
    public ApiResult<AiChatResponse> chat(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AiChatRequest request) {
        return ApiResult.success(aiAdvisorService.chat(userId, request));
    }

    @Operation(summary = "AI聊天（流式SSE）")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AiChatRequest request) {
        return aiAdvisorService.chatStream(userId, request);
    }

    @Operation(summary = "推荐问题列表")
    @GetMapping("/suggestions")
    public ApiResult<List<String>> suggestions() {
        return ApiResult.success(aiAdvisorService.getSuggestions());
    }

    @Operation(summary = "查询我的长期记忆")
    @GetMapping("/memory")
    public ApiResult<Map<String, Object>> getMemory(
            @AuthenticationPrincipal Long userId) {
        return ApiResult.success(aiAdvisorService.getMemory(userId));
    }

    @Operation(summary = "清除我的长期记忆")
    @DeleteMapping("/memory")
    public ApiResult<Void> clearMemory(
            @AuthenticationPrincipal Long userId) {
        aiAdvisorService.clearMemory(userId);
        return ApiResult.success(null);
    }
}
