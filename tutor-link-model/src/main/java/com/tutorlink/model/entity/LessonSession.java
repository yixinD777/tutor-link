package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lesson_session")
public class LessonSession extends MpBaseEntity {

    private Long scheduleId;
    private Long orderId;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime actualStart;
    private LocalTime actualEnd;
    private Integer status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer parentConfirm;
    private String remark;
}
