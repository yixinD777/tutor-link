package com.tutorlink.service.order;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.model.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

/**
 * 订单超时自动取消
 * 使用 Redis ZSET 实现延迟任务:
 * - key: tutorlink:order:expire
 * - score: 过期时间戳 (毫秒)
 * - value: orderId
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpireScheduler {

    private static final String ORDER_EXPIRE_KEY = "tutorlink:order:expire";
    private static final long EXPIRE_MINUTES = 30;

    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;
    private final OrderStateMachine orderStateMachine;

    /**
     * 添加订单过期延迟任务
     * 在订单变为 CONFIRMED 状态后调用
     */
    public void addExpireTask(Long orderId) {
        long expireAt = Instant.now().plusSeconds(EXPIRE_MINUTES * 60).toEpochMilli();
        redisTemplate.opsForZSet().add(ORDER_EXPIRE_KEY, String.valueOf(orderId), expireAt);
        log.info("Added expire task: orderId={}, expireAt={}", orderId, expireAt);
    }

    /**
     * 移除过期任务 (订单支付后调用)
     */
    public void removeExpireTask(Long orderId) {
        redisTemplate.opsForZSet().remove(ORDER_EXPIRE_KEY, String.valueOf(orderId));
    }

    /**
     * 每秒扫描已过期的订单，自动取消
     */
    @Scheduled(fixedDelay = 1000)
    public void checkExpiredOrders() {
        long now = Instant.now().toEpochMilli();
        Set<String> expiredOrderIds = redisTemplate.opsForZSet()
                .rangeByScore(ORDER_EXPIRE_KEY, 0, now);

        if (expiredOrderIds == null || expiredOrderIds.isEmpty()) {
            return;
        }

        for (String orderIdStr : expiredOrderIds) {
            Long orderId = Long.parseLong(orderIdStr);
            try {
                Order order = orderMapper.selectById(orderId);
                if (order != null && order.getStatus() == OrderStatus.CONFIRMED.getCode()) {
                    // 乐观锁更新状态为已取消
                    int updated = orderMapper.update(null,
                            new LambdaUpdateWrapper<Order>()
                                    .eq(Order::getId, orderId)
                                    .eq(Order::getStatus, OrderStatus.CONFIRMED.getCode())
                                    .set(Order::getStatus, OrderStatus.CANCELLED.getCode())
                                    .set(Order::getCancelReason, "超时未支付，自动取消"));
                    if (updated > 0) {
                        log.info("Order auto-cancelled due to timeout: orderId={}", orderId);
                        // 发布取消事件到发件箱
                        orderStateMachine.cancelOrder(orderId, 0L, 3,
                                new com.tutorlink.model.dto.order.OrderCancelRequest());
                    }
                }
                // 无论取消成功与否，移除 ZSET 中的记录
                redisTemplate.opsForZSet().remove(ORDER_EXPIRE_KEY, orderIdStr);
            } catch (Exception e) {
                log.error("Failed to auto-cancel order: orderId={}", orderId, e);
            }
        }
    }
}
