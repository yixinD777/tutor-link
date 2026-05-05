package com.tutorlink.model.dto.admin;

import lombok.Data;

@Data
public class AdminUserQueryRequest {

    private Integer role;

    private Integer status;

    private String keyword;

    private int page = 1;

    private int size = 20;
}
