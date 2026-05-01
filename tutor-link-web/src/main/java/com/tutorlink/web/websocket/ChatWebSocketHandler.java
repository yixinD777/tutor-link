package com.tutorlink.web.websocket;

import com.tutorlink.model.entity.ChatMessage;
import com.tutorlink.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final ChatService chatService;

    /**
     * 客户端发送消息: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        Long senderId = Long.parseLong(principal.getName());
        Long receiverId = Long.parseLong(payload.get("receiverId").toString());
        Integer msgType = Integer.parseInt(payload.getOrDefault("msgType", 1).toString());
        String content = payload.get("content").toString();

        ChatMessage msg = chatService.sendMessage(senderId, receiverId, msgType, content);
        log.info("WS message sent: sender={}, receiver={}, convId={}", senderId, receiverId, msg.getConversationId());
    }
}
