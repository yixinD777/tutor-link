package com.tutorlink.service.order;

import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.dao.mapper.OrderLogMapper;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.dao.mapper.OutboxMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tutorlink.mq.message.OrderEventMessage;
import com.tutorlink.model.dto.order.OrderCancelRequest;
import com.tutorlink.model.dto.order.OrderCreateRequest;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.OrderLog;
import com.tutorlink.model.entity.OutboxMessage;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStateMachine {

    private final OrderMapper orderMapper;
    private final OrderLogMapper orderLogMapper;
    private final OutboxMessageMapper outboxMessageMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 创建订单 (PARENT 发布需求)
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderCreateRequest request, Long parentUserId) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setParentUserId(parentUserId);
        order.setSubjectId(request.getSubjectId());
        order.setGrade(request.getGrade());
        order.setTitle(request.getTitle());
        order.setDescription(request.getDescription());
        order.setTeachingAddress(request.getTeachingAddress());
        order.setLongitude(request.getLongitude());
        order.setLatitude(request.getLatitude());
        order.setTeachingMode(request.getTeachingMode());
        order.setSchedule(request.getSchedule());
        order.setHourlyRate(request.getHourlyRate());
        order.setTotalHours(request.getTotalHours());
        order.setTotalAmount(request.getHourlyRate() * request.getTotalHours());
        order.setStatus(OrderStatus.PENDING.getCode());
        orderMapper.insert(order);

        // 写入日志
        insertOrderLog(order.getId(), null, OrderStatus.PENDING.getCode(),
                parentUserId, 1, "CREATE", "家长发布家教需求");

        // 写入发件箱
        publishOutboxEvent(order, null, OrderStatus.PENDING, "ORDER_CREATED", parentUserId, 1);

        return order;
    }

    /**
     * 家教表达意向 (PENDING → INTERESTED)
     */
    @Transactional(rollbackFor = Exception.class)
    public void expressInterest(Long orderId, Long tutorUserId) {
        Order order = getOrderOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.INTERESTED);

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, OrderStatus.PENDING.getCode())
                        .set(Order::getStatus, OrderStatus.INTERESTED.getCode())
                        .set(Order::getTutorUserId, tutorUserId));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "订单状态已变更，请刷新重试");
        }

        order.setStatus(OrderStatus.INTERESTED.getCode());
        order.setTutorUserId(tutorUserId);

        insertOrderLog(orderId, OrderStatus.PENDING.getCode(), OrderStatus.INTERESTED.getCode(),
                tutorUserId, 2, "EXPRESS_INTEREST", "家教表达意向");

        publishOutboxEvent(order, OrderStatus.PENDING, OrderStatus.INTERESTED, "ORDER_INTERESTED", tutorUserId, 2);
    }

    /**
     * 家长确认委托 (INTERESTED → CONFIRMED)
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmDelegation(Long orderId, Long parentUserId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getParentUserId().equals(parentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        validateTransition(order.getStatus(), OrderStatus.CONFIRMED);

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, OrderStatus.INTERESTED.getCode())
                        .set(Order::getStatus, OrderStatus.CONFIRMED.getCode())
                        .set(Order::getConfirmTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "订单状态已变更，请刷新重试");
        }

        order.setStatus(OrderStatus.CONFIRMED.getCode());
        order.setConfirmTime(LocalDateTime.now());

        insertOrderLog(orderId, OrderStatus.INTERESTED.getCode(), OrderStatus.CONFIRMED.getCode(),
                parentUserId, 1, "CONFIRM_DELEGATION", "家长确认委托");

        publishOutboxEvent(order, OrderStatus.INTERESTED, OrderStatus.CONFIRMED, "ORDER_CONFIRMED", parentUserId, 1);
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long operatorId, int operatorType, OrderCancelRequest request) {
        Order order = getOrderOrThrow(orderId);
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        // 付款前可直接取消，付款后需走退款
        if (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.IN_PROGRESS) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL, "已支付订单请申请退款");
        }

        int newStatusCode = OrderStatus.CANCELLED.getCode();
        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, order.getStatus())
                        .set(Order::getStatus, newStatusCode)
                        .set(Order::getCancelBy, operatorId)
                        .set(Order::getCancelReason, request != null ? request.getCancelReason() : null));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(newStatusCode);
        order.setCancelBy(operatorId);
        order.setCancelReason(request != null ? request.getCancelReason() : null);

        insertOrderLog(orderId, newStatusCode, newStatusCode,
                operatorId, operatorType, "CANCEL", "取消订单");

        publishOutboxEvent(order, currentStatus, OrderStatus.CANCELLED, "ORDER_CANCELLED", operatorId, operatorType);
    }

    /**
     * 标记开始上课
     */
    @Transactional(rollbackFor = Exception.class)
    public void startOrder(Long orderId, Long tutorUserId) {
        Order order = getOrderOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.IN_PROGRESS);

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, OrderStatus.PAID.getCode())
                        .set(Order::getStatus, OrderStatus.IN_PROGRESS.getCode())
                        .set(Order::getStartTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(OrderStatus.IN_PROGRESS.getCode());
        order.setStartTime(LocalDateTime.now());

        insertOrderLog(orderId, OrderStatus.PAID.getCode(), OrderStatus.IN_PROGRESS.getCode(),
                tutorUserId, 2, "START", "家教开始上课");

        publishOutboxEvent(order, OrderStatus.PAID, OrderStatus.IN_PROGRESS, "ORDER_STARTED", tutorUserId, 2);
    }

    /**
     * 家长确认完成
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long parentUserId) {
        Order order = getOrderOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.COMPLETED);

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, OrderStatus.IN_PROGRESS.getCode())
                        .set(Order::getStatus, OrderStatus.COMPLETED.getCode())
                        .set(Order::getCompleteTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now());

        insertOrderLog(orderId, OrderStatus.IN_PROGRESS.getCode(), OrderStatus.COMPLETED.getCode(),
                parentUserId, 1, "COMPLETE", "家长确认课程完成");

        publishOutboxEvent(order, OrderStatus.IN_PROGRESS, OrderStatus.COMPLETED, "ORDER_COMPLETED", parentUserId, 1);
    }

    /**
     * 开始试课：CONFIRMED -> TRIAL
     */
    @Transactional(rollbackFor = Exception.class)
    public void startTrial(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.TRIAL);

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, order.getStatus())
                        .set(Order::getStatus, OrderStatus.TRIAL.getCode()));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "操作失败，请重试");
        }

        insertOrderLog(orderId, order.getStatus(), OrderStatus.TRIAL.getCode(),
                order.getTutorUserId(), 2, "START_TRIAL", "开始试课");

        publishOutboxEvent(order, OrderStatus.fromCode(order.getStatus()), OrderStatus.TRIAL,
                "ORDER_TRIAL_STARTED", order.getTutorUserId(), 2);
    }

    /**
     * 试课通过：TRIAL -> CONFIRMED（回到待付款）
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmTrialPass(Long orderId, Long parentUserId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getParentUserId().equals(parentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.TRIAL.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "当前状态不是试课中");
        }

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, OrderStatus.TRIAL.getCode())
                        .set(Order::getStatus, OrderStatus.CONFIRMED.getCode()));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "操作失败，请重试");
        }

        insertOrderLog(orderId, OrderStatus.TRIAL.getCode(), OrderStatus.CONFIRMED.getCode(),
                parentUserId, 1, "TRIAL_PASS", "试课通过，等待付款");

        publishOutboxEvent(order, OrderStatus.TRIAL, OrderStatus.CONFIRMED,
                "ORDER_TRIAL_PASSED", parentUserId, 1);
    }

    /**
     * 试课不通过：TRIAL -> CANCELLED
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmTrialFail(Long orderId, Long parentUserId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getParentUserId().equals(parentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.TRIAL.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "当前状态不是试课中");
        }

        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, OrderStatus.TRIAL.getCode())
                        .set(Order::getStatus, OrderStatus.CANCELLED.getCode())
                        .set(Order::getCancelBy, parentUserId)
                        .set(Order::getCancelReason, "试课不通过"));
        if (updated == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "操作失败，请重试");
        }

        insertOrderLog(orderId, OrderStatus.TRIAL.getCode(), OrderStatus.CANCELLED.getCode(),
                parentUserId, 1, "TRIAL_FAIL", "试课不通过，订单取消");

        publishOutboxEvent(order, OrderStatus.TRIAL, OrderStatus.CANCELLED,
                "ORDER_TRIAL_FAILED", parentUserId, 1);
    }

    // ==================== 私有方法 ====================

    private Order getOrderOrThrow(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private void validateTransition(int currentStatus, OrderStatus targetStatus) {
        OrderStatus current = OrderStatus.fromCode(currentStatus);
        boolean valid = switch (targetStatus) {
            case INTERESTED -> current == OrderStatus.PENDING;
            case CONFIRMED -> current == OrderStatus.INTERESTED;
            case IN_PROGRESS -> current == OrderStatus.PAID;
            case COMPLETED -> current == OrderStatus.IN_PROGRESS;
            case CANCELLED -> current == OrderStatus.PENDING || current == OrderStatus.INTERESTED || current == OrderStatus.CONFIRMED;
            case TRIAL -> current == OrderStatus.CONFIRMED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "无法从 " + current.getDesc() + " 转换到 " + targetStatus.getDesc());
        }
    }

    private void insertOrderLog(Long orderId, Integer fromStatus, Integer toStatus,
                                 Long operatorId, int operatorType, String action, String remark) {
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setFromStatus(fromStatus);
        orderLog.setToStatus(toStatus);
        orderLog.setOperatorId(operatorId);
        orderLog.setOperatorType(operatorType);
        orderLog.setAction(action);
        orderLog.setRemark(remark);
        orderLogMapper.insert(orderLog);
    }

    private void publishOutboxEvent(Order order, OrderStatus fromStatus, OrderStatus toStatus,
                                     String eventType, Long operatorId, int operatorType) {
        OrderEventMessage event = OrderEventMessage.builder()
                .eventId(String.valueOf(com.tutorlink.common.util.SnowflakeIdUtil.nextId()))
                .eventType(eventType)
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .fromStatus(fromStatus != null ? fromStatus.getCode() : null)
                .toStatus(toStatus.getCode())
                .operatorId(operatorId)
                .operatorType(operatorType)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            OutboxMessage outbox = new OutboxMessage();
            outbox.setTopic("order-events");
            outbox.setRoutingKey(eventType.toLowerCase().replace("_", "."));
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outbox.setStatus(0); // PENDING
            outbox.setRetryCount(0);
            outbox.setMaxRetries(5);
            outboxMessageMapper.insert(outbox);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event: orderId={}", order.getId(), e);
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "TL" + timestamp + random;
    }
}
