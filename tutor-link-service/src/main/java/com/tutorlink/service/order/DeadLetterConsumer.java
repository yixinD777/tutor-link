package com.tutorlink.service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.mq.message.OrderEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 死信消费者 - 处理 outbox-retry Topic
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "outbox-retry", groupId = "dead-letter-group")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.error("Dead letter received: key={}, offset={}", record.key(), record.offset());

            OrderEventMessage event = objectMapper.readValue(record.value(), OrderEventMessage.class);
            log.error("Dead letter event: orderId={}, eventType={}, eventId={}",
                    event.getOrderId(), event.getEventType(), event.getEventId());

            // TODO: 写入告警表，通知管理员
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Dead letter consumer error", e);
        }
    }
}
