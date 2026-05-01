package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbox_message")
public class OutboxMessage extends MpBaseEntity {

    private String topic;

    private String routingKey;

    private String payload;

    private Integer status;

    private Integer retryCount;

    private Integer maxRetries;
}
