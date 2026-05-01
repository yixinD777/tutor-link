package com.tutorlink.model.dto.order;

import lombok.Data;

@Data
public class OrderCancelRequest {

    private String cancelReason;
}
