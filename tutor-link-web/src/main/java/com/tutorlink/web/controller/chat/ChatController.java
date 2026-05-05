package com.tutorlink.web.controller.chat;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.entity.ChatConversation;
import com.tutorlink.model.entity.ChatMessage;
import com.tutorlink.service.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "聊天")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "我的会话列表")
    @GetMapping("/conversations")
    public ApiResult<List<ChatConversation>> listConversations(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(chatService.listConversations(userId));
    }

    @Operation(summary = "会话消息历史")
    @GetMapping("/conversations/{id}/messages")
    public ApiResult<List<ChatMessage>> listMessages(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResult.success(chatService.listMessages(id, userId, limit));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/conversations/{id}/read")
    public ApiResult<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        chatService.markAsRead(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/unread-count")
    public ApiResult<Integer> getUnreadCount(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(chatService.getUnreadCount(userId));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public ApiResult<ChatMessage> sendMessage(
            @AuthenticationPrincipal Long userId,
            @RequestBody java.util.Map<String, Object> body) {
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        Integer msgType = body.containsKey("msgType") ? Integer.valueOf(body.get("msgType").toString()) : 1;
        String content = body.get("content").toString();
        ChatMessage msg = chatService.sendMessage(userId, receiverId, msgType, content);
        chatService.pushWebSocketMessage(receiverId, msg);
        return ApiResult.success(msg);
    }
}
