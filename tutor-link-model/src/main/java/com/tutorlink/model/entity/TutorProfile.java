package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tutor_profile")
public class TutorProfile extends MpBaseEntity {

    private Long userId;

    private String realName;

    private String university;

    private String major;

    private Integer enrollmentYear;

    private Integer educationLevel;

    private Integer certificationStatus;

    private LocalDateTime certificationTime;

    private String intro;

    private String teachingStyle;

    private Integer hourlyRateMin;

    private Integer hourlyRateMax;

    private BigDecimal avgRating;

    private Integer ratingCount;

    private Integer orderCount;

    private String province;

    private String city;

    private String district;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer isOnline;

    /** 从 user 表关联，非数据库列 */
    @TableField(exist = false)
    private String avatarUrl;

    /** 从 user 表关联，非数据库列 */
    @TableField(exist = false)
    private String nickname;
}
