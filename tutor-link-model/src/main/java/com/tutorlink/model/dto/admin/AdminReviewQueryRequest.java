package com.tutorlink.model.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReviewQueryRequest {

    private Integer rating;

    private Integer reviewerRole;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int page = 1;

    private int size = 20;
}
