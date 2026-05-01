package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation")
public class ChatConversation extends MpBaseEntity {

    private String conversationNo;

    private Long userAId;

    private Long userBId;

    private Long lastMessageId;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    private Integer userAUnread;

    private Integer userBUnread;
}
