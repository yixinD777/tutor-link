package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_schedule")
public class CourseSchedule extends MpBaseEntity {

    private Long orderId;
    private Long parentUserId;
    private Long tutorUserId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String teachingAddress;
    private Integer teachingMode;
    private Integer hourlyRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveUntil;
    private Integer status;
}
