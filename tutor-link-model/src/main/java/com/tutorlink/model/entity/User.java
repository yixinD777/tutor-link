package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends MpBaseEntity {

    private String phone;

    private String phoneHash;

    private String password;

    private String wxOpenid;

    private String wxUnionid;

    private String nickname;

    private String avatarUrl;

    private Integer gender;

    private Integer role;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;
}
