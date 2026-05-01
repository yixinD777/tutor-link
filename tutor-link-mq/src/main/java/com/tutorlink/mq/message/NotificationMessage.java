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
public class NotificationMessage {

    private String eventId;

    private Long userId;

    private Integer type;

    private String title;

    private String content;

    private Long relatedId;

    private LocalDateTime timestamp;
}
