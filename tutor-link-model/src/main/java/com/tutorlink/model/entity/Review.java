package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review")
public class Review extends MpBaseEntity {

    private Long orderId;

    private Long reviewerId;

    private Long revieweeId;

    private Integer reviewerRole;

    private Integer rating;

    private String content;

    private String tags;

    private Integer isAnonymous;
}
