package com.tutorlink.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.mq.message.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendNotification(NotificationMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            String key = String.valueOf(message.getUserId());
            kafkaTemplate.send(TOPIC, key, json);
            log.info("Notification event sent: userId={}, type={}", message.getUserId(), message.getType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification: userId={}", message.getUserId(), e);
            throw new RuntimeException("Failed to serialize notification", e);
        }
    }
}
