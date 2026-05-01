package com.tutorlink.common.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {

    private Long id;

    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
