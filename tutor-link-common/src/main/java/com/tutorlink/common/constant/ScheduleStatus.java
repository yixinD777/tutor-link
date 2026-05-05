package com.tutorlink.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleStatus {

    PENDING(0, "待确认"),
    ACTIVE(1, "生效"),
    PAUSED(2, "暂停"),
    ENDED(3, "已结束");

    private final int code;
    private final String desc;

    public static ScheduleStatus fromCode(int code) {
        for (ScheduleStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ScheduleStatus code: " + code);
    }
}
