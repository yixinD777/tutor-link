package com.tutorlink.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.mq.message.OrderEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderEvent(OrderEventMessage event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = String.valueOf(event.getOrderId());

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(TOPIC, key, json);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send order event: orderId={}, eventType={}",
                            event.getOrderId(), event.getEventType(), ex);
                } else {
                    log.info("Order event sent: orderId={}, eventType={}, partition={}, offset={}",
                            event.getOrderId(), event.getEventType(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order event: orderId={}", event.getOrderId(), e);
            throw new RuntimeException("Failed to serialize order event", e);
        }
    }

    public void sendDelayEvent(OrderEventMessage event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = String.valueOf(event.getOrderId());
            kafkaTemplate.send("order-delay", key, json);
            log.info("Delay event sent: orderId={}, eventType={}", event.getOrderId(), event.getEventType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize delay event: orderId={}", event.getOrderId(), e);
            throw new RuntimeException("Failed to serialize delay event", e);
        }
    }
}
