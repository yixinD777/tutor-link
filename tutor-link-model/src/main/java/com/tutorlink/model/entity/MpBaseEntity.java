package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 增强基类，包含 MP 注解
 * 所有实体类继承此类
 */
@Data
public abstract class MpBaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
