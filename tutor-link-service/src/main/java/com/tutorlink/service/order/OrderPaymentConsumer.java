package com.tutorlink.service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorlink.mq.message.OrderEventMessage;
import com.tutorlink.service.payment.PaymentService;
import com.tutorlink.service.payment.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 订单支付消费者 - 放在 service 模块以访问业务层
 * Consumer Group: payment-group
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            OrderEventMessage event = objectMapper.readValue(record.value(), OrderEventMessage.class);
            log.info("Payment consumer: orderId={}, eventType={}", event.getOrderId(), event.getEventType());

            switch (event.getEventType()) {
                case "ORDER_CONFIRMED" -> {
                    paymentService.createPayment(event.getOrderId());
                    log.info("Payment record created: orderId={}", event.getOrderId());
                }
                case "ORDER_COMPLETED" -> {
                    paymentService.releaseToTutor(event.getOrderId());
                    log.info("Payment released: orderId={}", event.getOrderId());
                }
                default -> log.debug("Payment: ignored event {}", event.getEventType());
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Payment consumer error: offset={}", record.offset(), e);
        }
    }
}
