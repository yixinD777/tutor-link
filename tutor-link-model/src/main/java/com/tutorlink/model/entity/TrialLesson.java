package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trial_lesson")
public class TrialLesson extends MpBaseEntity {

    private Long orderId;
    private Long parentUserId;
    private Long tutorUserId;
    private LocalDateTime trialDate;
    private Integer trialDuration;
    private Integer trialPrice;
    private String trialAddress;
    private Integer trialMode;
    private Integer status;
    private String parentFeedback;
    private Integer result;
}
