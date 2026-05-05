package com.tutorlink.model.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOrderQueryRequest {

    private Integer status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer minAmount;

    private Integer maxAmount;

    private String keyword;

    private int page = 1;

    private int size = 20;
}
