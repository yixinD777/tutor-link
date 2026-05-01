package com.tutorlink.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING(1, "待确认"),
    CONFIRMED(2, "已确认"),
    PAID(3, "已支付"),
    IN_PROGRESS(4, "进行中"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    REFUNDING(7, "退款中"),
    REFUNDED(8, "已退款"),
    DISPUTED(9, "争议中");

    private final int code;
    private final String desc;

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}
