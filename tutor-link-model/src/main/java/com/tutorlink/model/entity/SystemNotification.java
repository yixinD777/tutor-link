package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_notification")
public class SystemNotification extends MpBaseEntity {

    private Long userId;

    private Integer type;

    private String title;

    private String content;

    private Long relatedId;

    private Integer isRead;
}
