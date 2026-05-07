package com.tutorlink.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PHONE_ALREADY_BOUND(1003, "手机号已被绑定"),
    INVALID_SMS_CODE(1004, "验证码无效"),
    WX_LOGIN_FAILED(1005, "微信登录失败"),
    ACCOUNT_ALREADY_EXISTS(1006, "账号已存在"),
    INVALID_PASSWORD(1007, "密码错误"),

    // 认证相关 2xxx
    CERTIFICATION_PENDING(2001, "认证审核中"),
    CERTIFICATION_REJECTED(2002, "认证未通过"),
    CERTIFICATION_NOT_FOUND(2003, "认证记录不存在"),

    // 订单相关 3xxx
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_INVALID(3002, "订单状态不允许此操作"),
    ORDER_ALREADY_ACCEPTED(3003, "订单已被接单"),
    ORDER_CANNOT_CANCEL(3004, "订单无法取消"),

    // 支付相关 4xxx
    PAYMENT_FAILED(4001, "支付失败"),
    PAYMENT_NOT_FOUND(4002, "支付记录不存在"),
    REFUND_FAILED(4003, "退款失败"),

    // 评价相关 5xxx
    REVIEW_ALREADY_EXISTS(5001, "已评价过"),
    REVIEW_ORDER_NOT_COMPLETED(5002, "订单未完成，无法评价"),

    // AI顾问相关 6xxx
    AI_SERVICE_ERROR(6001, "AI服务异常"),
    AI_RATE_LIMITED(6002, "AI请求过于频繁"),
    AI_NOT_CONFIGURED(6003, "AI服务未配置"),

    // 排期相关 7xxx
    SCHEDULE_NOT_FOUND(7001, "排期不存在"),
    SCHEDULE_STATUS_INVALID(7002, "排期状态不允许此操作"),
    SCHEDULE_CANNOT_CONFIRM_OWN(7004, "不能确认自己创建的排期"),
    SCHEDULE_TIME_CONFLICT(7005, "排期时间冲突");

    private final int code;
    private final String message;
}
