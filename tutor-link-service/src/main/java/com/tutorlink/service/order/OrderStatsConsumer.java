package com.tutorlink.service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.mq.message.OrderEventMessage;
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
 * 订单统计消费者 - 放在 service 模块以访问业务层
 * Consumer Group: stats-group
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatsConsumer {

    private final ObjectMapper objectMapper;
    private final TutorProfileMapper tutorProfileMapper;

    @KafkaListener(topics = "order-events", groupId = "stats-group")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            OrderEventMessage event = objectMapper.readValue(record.value(), OrderEventMessage.class);
            log.info("Stats consumer: orderId={}, eventType={}", event.getOrderId(), event.getEventType());

            if ("ORDER_COMPLETED".equals(event.getEventType()) && event.getOperatorId() != null) {
                tutorProfileMapper.update(null,
                        new LambdaUpdateWrapper<TutorProfile>()
                                .eq(TutorProfile::getUserId, event.getOperatorId())
                                .setSql("order_count = order_count + 1"));
                log.info("Tutor order count incremented: userId={}", event.getOperatorId());
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Stats consumer error: offset={}", record.offset(), e);
        }
    }
}
