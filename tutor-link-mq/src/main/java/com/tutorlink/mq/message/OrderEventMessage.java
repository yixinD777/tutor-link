package com.tutorlink.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventMessage {

    private String eventId;

    private String eventType;

    private Long orderId;

    private String orderNo;

    private Integer fromStatus;

    private Integer toStatus;

    private Long operatorId;

    private Integer operatorType;

    private LocalDateTime timestamp;

    private String payload;
}
