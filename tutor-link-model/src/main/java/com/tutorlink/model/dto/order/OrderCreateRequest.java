package com.tutorlink.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateRequest {

    @NotNull(message = "科目不能为空")
    private Long subjectId;

    @NotBlank(message = "年级不能为空")
    private String grade;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    private String teachingAddress;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @NotNull(message = "授课方式不能为空")
    private Integer teachingMode;

    private String schedule;

    @NotNull(message = "时薪不能为空")
    private Integer hourlyRate;

    @NotNull(message = "总课时不能为空")
    private Integer totalHours;
}
