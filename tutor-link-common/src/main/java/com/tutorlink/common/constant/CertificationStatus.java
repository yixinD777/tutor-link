package com.tutorlink.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CertificationStatus {

    NONE(0, "未认证"),
    PENDING(1, "审核中"),
    APPROVED(2, "已认证"),
    REJECTED(3, "已拒绝");

    private final int code;
    private final String desc;

    public static CertificationStatus fromCode(int code) {
        for (CertificationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CertificationStatus code: " + code);
    }
}
