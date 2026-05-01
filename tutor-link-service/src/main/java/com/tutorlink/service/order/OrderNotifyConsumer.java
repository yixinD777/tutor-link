package com.tutorlink.service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.mq.message.OrderEventMessage;
import com.tutorlink.service.notification.NotificationService;
import com.tutorlink.service.payment.PaymentService;
import com.tutorlink.service.payment.RefundService;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.model.entity.TutorProfile;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 订单事件通知消费者 - 放在 service 模块以访问业务层
 * Consumer Group: notify-group
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotifyConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "notify-group")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            OrderEventMessage event = objectMapper.readValue(record.value(), OrderEventMessage.class);
            log.info("Notify consumer: orderId={}, eventType={}", event.getOrderId(), event.getEventType());

            String title = switch (event.getEventType()) {
                case "ORDER_CREATED" -> "新家教需求";
                case "ORDER_CONFIRMED" -> "家教已接单";
                case "ORDER_CANCELLED" -> "订单已取消";
                case "ORDER_STARTED" -> "课程已开始";
                case "ORDER_COMPLETED" -> "课程已完成";
                default -> "订单状态变更";
            };

            String content = switch (event.getEventType()) {
                case "ORDER_CREATED" -> "有家长发布了新的家教需求";
                case "ORDER_CONFIRMED" -> "家教已确认接单，请尽快完成支付";
                case "ORDER_CANCELLED" -> "订单已取消";
                case "ORDER_STARTED" -> "家教已确认开始上课";
                case "ORDER_COMPLETED" -> "课程已完成，请确认并评价";
                default -> "订单状态已更新";
            };

            notificationService.sendNotification(event.getOperatorId(), 1, title, content, event.getOrderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Notify consumer error: offset={}", record.offset(), e);
        }
    }
}
