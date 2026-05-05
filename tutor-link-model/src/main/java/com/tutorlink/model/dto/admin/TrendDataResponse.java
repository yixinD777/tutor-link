package com.tutorlink.model.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataResponse {

    private List<String> labels;

    private List<Long> counts;

    private List<BigDecimal> amounts;
}
