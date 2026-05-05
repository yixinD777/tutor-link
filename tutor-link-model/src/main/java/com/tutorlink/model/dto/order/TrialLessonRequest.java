package com.tutorlink.model.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrialLessonRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "试课时间不能为空")
    private LocalDateTime trialDate;

    @NotNull(message = "试课时长不能为空")
    private Integer trialDuration;

    @NotNull(message = "试课价格不能为空")
    private Integer trialPrice;

    private String trialAddress;

    @NotNull(message = "授课方式不能为空")
    private Integer trialMode;
}
