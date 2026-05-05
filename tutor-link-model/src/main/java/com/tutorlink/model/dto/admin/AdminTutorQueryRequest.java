package com.tutorlink.model.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminTutorQueryRequest {

    private Integer certificationStatus;

    private BigDecimal minRating;

    private String university;

    private String keyword;

    private int page = 1;

    private int size = 20;
}
