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
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @AuthenticationPrincipal Long userId,
            @RequestParam String message,
            @RequestParam(required = false) String conversationId) {
        AiChatRequest request = new AiChatRequest();
        request.setMessage(message);
        request.setConversationId(conversationId);
        return aiAdvisorService.chatStream(userId, request);
    }

    @Operation(summary = "推荐问题列表")
    @GetMapping("/suggestions")
    public ApiResult<List<String>> suggestions() {
        return ApiResult.success(aiAdvisorService.getSuggestions());
    }
}
