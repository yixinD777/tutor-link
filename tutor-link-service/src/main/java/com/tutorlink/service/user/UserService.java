package com.tutorlink.service.user;

import com.tutorlink.model.dto.user.LoginResponse;
import com.tutorlink.model.dto.user.PasswordLoginRequest;
import com.tutorlink.model.dto.user.PhoneLoginRequest;
import com.tutorlink.model.dto.user.RegisterRequest;
import com.tutorlink.model.dto.user.WxLoginRequest;
import com.tutorlink.model.entity.User;

public interface UserService {

    LoginResponse register(RegisterRequest request);

    LoginResponse passwordLogin(PasswordLoginRequest request);

    LoginResponse phoneLogin(PhoneLoginRequest request);

    LoginResponse wxLogin(WxLoginRequest request);

    LoginResponse refreshToken(String refreshToken);

    void logout(Long userId, String jti);

    User getUserById(Long userId);

    User getUserByPhone(String phone);

    void updateLastLogin(Long userId, String ip);

    void updateAvatar(Long userId, String avatarUrl);
}
