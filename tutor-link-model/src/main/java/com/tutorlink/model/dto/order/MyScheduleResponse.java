package com.tutorlink.model.dto.order;

import com.tutorlink.model.entity.CourseSchedule;
import lombok.Data;

import java.util.List;

@Data
public class MyScheduleResponse {
    private Long orderId;
    private String orderTitle;
    private Long counterpartyUserId;
    private String counterpartyNickname;
    private String counterpartyAvatarUrl;
    private List<CourseSchedule> schedules;
}
