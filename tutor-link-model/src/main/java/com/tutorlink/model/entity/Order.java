package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_main")
public class Order extends MpBaseEntity {

    private String orderNo;

    private Long parentUserId;

    private Long tutorUserId;

    private Long subjectId;

    private String grade;

    private String title;

    private String description;

    private String teachingAddress;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer teachingMode;

    private String schedule;

    private Integer hourlyRate;

    private Integer totalHours;

    private Integer totalAmount;

    private Integer status;

    private String cancelReason;

    private Long cancelBy;

    private LocalDateTime confirmTime;

    private LocalDateTime startTime;

    private LocalDateTime completeTime;
}
