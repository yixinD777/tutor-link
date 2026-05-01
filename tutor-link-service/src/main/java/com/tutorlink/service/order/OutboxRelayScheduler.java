package com.tutorlink.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.dao.mapper.OutboxMessageMapper;
import com.tutorlink.mq.producer.OrderEventProducer;
import com.tutorlink.model.entity.OutboxMessage;
import com.tutorlink.mq.message.OrderEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 事务发件箱调度器
 * 定时扫描 outbox_message 表中 PENDING 状态的消息，发送到 Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxMessageMapper outboxMessageMapper;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    public void relayOutboxMessages() {
        List<OutboxMessage> pendingMessages = outboxMessageMapper.selectList(
                new LambdaQueryWrapper<OutboxMessage>()
                        .eq(OutboxMessage::getStatus, 0)
                        .lt(OutboxMessage::getRetryCount, 5)
                        .last("LIMIT 100"));

        for (OutboxMessage msg : pendingMessages) {
            try {
                OrderEventMessage event = objectMapper.readValue(msg.getPayload(), OrderEventMessage.class);
                orderEventProducer.sendOrderEvent(event);

                // 标记为已发送
                msg.setStatus(1);
                outboxMessageMapper.updateById(msg);
            } catch (Exception e) {
                log.error("Failed to relay outbox message: id={}", msg.getId(), e);
                // 增加重试计数
                msg.setRetryCount(msg.getRetryCount() + 1);
                if (msg.getRetryCount() >= msg.getMaxRetries()) {
                    msg.setStatus(2); // FAILED
                }
                outboxMessageMapper.updateById(msg);
            }
        }
    }
}
