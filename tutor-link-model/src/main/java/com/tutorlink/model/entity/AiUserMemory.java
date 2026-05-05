package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_user_memory")
public class AiUserMemory extends MpBaseEntity {

    private Long userId;

    /** AI 提炼的用户偏好摘要 */
    private String summary;

    /** 结构化事实列表（JSON 数组字符串） */
    private String facts;

    /** 触发本次更新的 conversationId */
    private String sourceConv;
}
