package com.tutorlink.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色 - 支持 bitmask 组合
 * 1=家长, 2=家教, 4=管理员
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    PARENT(1, "家长"),
    TUTOR(2, "家教"),
    ADMIN(4, "管理员");

    private final int code;
    private final String desc;

    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown UserRole code: " + code);
    }

    /**
     * 判断角色值是否包含指定角色 (bitmask)
     */
    public static boolean hasRole(int roleMask, UserRole target) {
        return (roleMask & target.code) != 0;
    }
}
