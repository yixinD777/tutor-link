package com.tutorlink.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginRequest {

    @NotBlank(message = "code不能为空")
    private String code;

    private String encryptedData;

    private String iv;
}
