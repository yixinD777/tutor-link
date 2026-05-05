package com.tutorlink.service.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.util.SnowflakeIdUtil;
import com.tutorlink.dao.mapper.ChatConversationMapper;
import com.tutorlink.dao.mapper.ChatMessageMapper;
import com.tutorlink.model.entity.ChatConversation;
import com.tutorlink.model.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 获取或创建会话
     */
    @Transactional
    public ChatConversation getOrCreateConversation(Long userAId, Long userBId) {
        // 保证 userA < userB 以维持唯一性
        long smallerId = Math.min(userAId, userBId);
        long largerId = Math.max(userAId, userBId);

        ChatConversation conv = conversationMapper.selectOne(
                new LambdaQueryWrapper<ChatConversation>()
                        .eq(ChatConversation::getUserAId, smallerId)
                        .eq(ChatConversation::getUserBId, largerId));

        if (conv == null) {
            conv = new ChatConversation();
            conv.setId(SnowflakeIdUtil.nextId());
            conv.setConversationNo("CONV" + System.currentTimeMillis());
            conv.setUserAId(smallerId);
            conv.setUserBId(largerId);
            conv.setUserAUnread(0);
            conv.setUserBUnread(0);
            conversationMapper.insert(conv);
            log.info("Created conversation: id={}, users={} & {}", conv.getId(), smallerId, largerId);
        }
        return conv;
    }

    /**
     * 发送消息 (WebSocket + 持久化)
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long senderId, Long receiverId, Integer msgType, String content) {
        ChatConversation conv = getOrCreateConversation(senderId, receiverId);

        ChatMessage msg = new ChatMessage();
        msg.setId(SnowflakeIdUtil.nextId());
        msg.setConversationId(conv.getId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setMsgType(msgType);
        msg.setContent(content);
        msg.setIsRead(0);
        messageMapper.insert(msg);

        // 更新会话最后消息和未读数
        boolean isUserA = senderId.equals(conv.getUserAId());
        LambdaUpdateWrapper<ChatConversation> updateWrapper = new LambdaUpdateWrapper<ChatConversation>()
                .eq(ChatConversation::getId, conv.getId())
                .set(ChatConversation::getLastMessageId, msg.getId())
                .set(ChatConversation::getLastMessageContent, truncate(content, 200))
                .set(ChatConversation::getLastMessageTime, LocalDateTime.now());

        if (isUserA) {
            updateWrapper.setSql("user_b_unread = user_b_unread + 1");
        } else {
            updateWrapper.setSql("user_a_unread = user_a_unread + 1");
        }
        conversationMapper.update(null, updateWrapper);

        return msg;
    }

    /**
     * 异步推送 WebSocket 消息（在事务提交后执行）
     */
    public void pushWebSocketMessage(Long receiverId, ChatMessage msg) {
        if (messagingTemplate == null) {
            log.warn("SimpMessagingTemplate is null, skipping WebSocket push");
            return;
        }
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/messages",
                    msg);
        } catch (Exception e) {
            log.warn("WebSocket push failed for user {}: {}", receiverId, e.getMessage());
        }
    }

    /**
     * 获取我的会话列表
     */
    public List<ChatConversation> listConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<ChatConversation>()
                        .and(w -> w.eq(ChatConversation::getUserAId, userId)
                                .or().eq(ChatConversation::getUserBId, userId))
                        .orderByDesc(ChatConversation::getLastMessageTime));
    }

    /**
     * 获取会话消息历史
     */
    public List<ChatMessage> listMessages(Long conversationId, Long userId, int limit) {
        // 验证用户属于该会话
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || (!conv.getUserAId().equals(userId) && !conv.getUserBId().equals(userId))) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByDesc(ChatMessage::getCreateTime)
                        .last("LIMIT " + limit));
    }

    /**
     * 标记消息已读
     */
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) return;

        // 标记消息已读
        messageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));

        // 清零未读数
        if (conv.getUserAId().equals(userId)) {
            conversationMapper.update(null, new LambdaUpdateWrapper<ChatConversation>()
                    .eq(ChatConversation::getId, conversationId)
                    .set(ChatConversation::getUserAUnread, 0));
        } else {
            conversationMapper.update(null, new LambdaUpdateWrapper<ChatConversation>()
                    .eq(ChatConversation::getId, conversationId)
                    .set(ChatConversation::getUserBUnread, 0));
        }
    }

    /**
     * 获取总未读消息数
     */
    public int getUnreadCount(Long userId) {
        List<ChatConversation> convs = conversationMapper.selectList(
                new LambdaQueryWrapper<ChatConversation>()
                        .and(w -> w.eq(ChatConversation::getUserAId, userId)
                                .or().eq(ChatConversation::getUserBId, userId)));

        int total = 0;
        for (ChatConversation conv : convs) {
            if (conv.getUserAId().equals(userId)) {
                total += conv.getUserAUnread();
            } else {
                total += conv.getUserBUnread();
            }
        }
        return total;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
