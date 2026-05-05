package com.tutorlink.model.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminRefundQueryRequest {

    private Integer status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int page = 1;

    private int size = 20;
}
