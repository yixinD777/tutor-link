package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profile")
public class UserProfile extends MpBaseEntity {

    private Long userId;

    private String realName;

    private String contactPhone;

    private String province;

    private String city;

    private String district;

    private String addressDetail;

    private BigDecimal longitude;

    private BigDecimal latitude;
}
