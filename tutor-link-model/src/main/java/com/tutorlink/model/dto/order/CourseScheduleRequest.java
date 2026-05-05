package com.tutorlink.model.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CourseScheduleRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "排期不能为空")
    private List<ScheduleItem> schedules;

    @Data
    public static class ScheduleItem {
        @NotNull(message = "星期几不能为空")
        private Integer dayOfWeek;

        @NotNull(message = "上课时间不能为空")
        private LocalTime startTime;

        @NotNull(message = "下课时间不能为空")
        private LocalTime endTime;

        private String teachingAddress;

        @NotNull(message = "授课方式不能为空")
        private Integer teachingMode;

        @NotNull(message = "课时费不能为空")
        private Integer hourlyRate;

        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveFrom;

        private LocalDate effectiveUntil;
    }
}
