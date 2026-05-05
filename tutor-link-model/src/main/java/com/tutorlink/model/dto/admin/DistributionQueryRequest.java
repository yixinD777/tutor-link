package com.tutorlink.model.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DistributionQueryRequest {

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}
