package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment")
public class Payment extends MpBaseEntity {

    private String paymentNo;

    private Long orderId;

    private Long payerUserId;

    private Long payeeUserId;

    private Integer amount;

    private Integer platformFee;

    private Integer tutorAmount;

    private Integer payChannel;

    private String wxPrepayId;

    private String wxTransactionId;

    private Integer status;

    private LocalDateTime paidTime;

    private LocalDateTime releasedTime;
}
