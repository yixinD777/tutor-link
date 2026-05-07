package com.tutorlink.common.util;

public class RedisKeyUtil {

    private static final String PREFIX = "tutorlink:";

    // ====== 用户相关 ======
    public static String refreshToken(Long userId) {
        return PREFIX + "refresh:user:" + userId;
    }

    public static String tokenBlacklist(String jti) {
        return PREFIX + "token:blacklist:" + jti;
    }

    public static String smsCode(String phone) {
        return PREFIX + "sms:code:" + phone;
    }

    // ====== 微信相关 ======
    public static String wxSessionKey(String openid) {
        return PREFIX + "wx:session:" + openid;
    }

    // ====== 限流相关 ======
    public static String rateLimit(String ip, String endpoint) {
        return PREFIX + "ratelimit:" + endpoint + ":" + ip;
    }

    // ====== 订单相关 ======
    public static String orderDetail(Long orderId) {
        return PREFIX + "order:detail:" + orderId;
    }

    // ====== 消费者幂等 ======
    public static String processedEvent(String eventId) {
        return PREFIX + "event:processed:" + eventId;
    }

    // ====== AI 会话 ======
    public static String aiConversation(Long userId, String conversationId) {
        return PREFIX + "ai:conversation:" + userId + ":" + conversationId;
    }

    // ====== AI 用户记忆 ======
    public static String aiUserMemory(Long userId) {
        return PREFIX + "ai:memory:" + userId;
    }
}
