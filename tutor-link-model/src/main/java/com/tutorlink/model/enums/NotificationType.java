package com.tutorlink.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    ORDER(1, "订单通知"),
    PAYMENT(2, "支付通知"),
    CERTIFICATION(3, "认证通知"),
    SYSTEM(4, "系统通知");

    private final int code;
    private final String desc;
}
