package com.tutorlink.model.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrendQueryRequest {

    private String granularity = "day";

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}
