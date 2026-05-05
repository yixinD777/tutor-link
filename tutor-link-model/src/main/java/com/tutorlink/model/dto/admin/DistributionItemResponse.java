package com.tutorlink.model.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionItemResponse {

    private String label;

    private Long count;

    private BigDecimal percentage;
}
