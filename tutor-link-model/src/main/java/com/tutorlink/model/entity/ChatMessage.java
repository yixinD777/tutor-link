package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessage extends MpBaseEntity {

    private Long conversationId;

    private Long senderId;

    private Long receiverId;

    private Integer msgType;

    private String content;

    private Integer isRead;
}
