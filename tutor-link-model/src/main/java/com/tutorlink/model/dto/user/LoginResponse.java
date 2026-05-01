package com.tutorlink.model.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer role;
}
