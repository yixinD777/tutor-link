package com.tutorlink.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStatus {

    PENDING(1, "待审核"),
    PROCESSING(2, "退款中"),
    SUCCESS(3, "退款成功"),
    FAILED(4, "退款失败");

    private final int code;
    private final String desc;
}
