package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund")
public class Refund extends MpBaseEntity {

    private String refundNo;

    private Long orderId;

    private Long paymentId;

    private Long applicantId;

    private Integer amount;

    private String reason;

    private String wxRefundId;

    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private LocalDateTime completeTime;
}
