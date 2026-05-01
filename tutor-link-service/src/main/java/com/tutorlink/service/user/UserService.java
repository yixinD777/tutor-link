package com.tutorlink.service.user;

import com.tutorlink.model.dto.user.LoginResponse;
import com.tutorlink.model.dto.user.PhoneLoginRequest;
import com.tutorlink.model.dto.user.WxLoginRequest;
import com.tutorlink.model.entity.User;

public interface UserService {

    LoginResponse phoneLogin(PhoneLoginRequest request);

    LoginResponse wxLogin(WxLoginRequest request);

    LoginResponse refreshToken(String refreshToken);

    void logout(Long userId, String jti);

    User getUserById(Long userId);

    User getUserByPhone(String phone);

    void updateLastLogin(Long userId, String ip);
}
