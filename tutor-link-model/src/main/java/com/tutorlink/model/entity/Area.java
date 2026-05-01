package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("area")
public class Area extends MpBaseEntity {

    private String name;

    private Long parentId;

    @TableField("`level`")
    private Integer level;

    private Integer sortOrder;
}
