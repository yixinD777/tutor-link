package com.tutorlink.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {

    PENDING(1, "待支付"),
    PAID(2, "已支付"),
    RELEASED(3, "已放款"),
    FROZEN(4, "已冻结");

    private final int code;
    private final String desc;
}
