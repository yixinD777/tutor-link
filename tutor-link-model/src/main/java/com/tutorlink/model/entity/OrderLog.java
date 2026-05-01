package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_log")
public class OrderLog extends MpBaseEntity {

    private Long orderId;

    private Integer fromStatus;

    private Integer toStatus;

    private Long operatorId;

    private Integer operatorType;

    private String action;

    private String remark;
}
