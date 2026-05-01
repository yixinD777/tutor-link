package com.tutorlink.web.controller.user;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.user.LoginResponse;
import com.tutorlink.model.dto.user.PhoneLoginRequest;
import com.tutorlink.model.dto.user.WxLoginRequest;
import com.tutorlink.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "手机号+验证码登录")
    @PostMapping("/phone-login")
    public ApiResult<LoginResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        return ApiResult.success(userService.phoneLogin(request));
    }

    @Operation(summary = "微信小程序登录")
    @PostMapping("/wx-login")
    public ApiResult<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResult.success(userService.wxLogin(request));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public ApiResult<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return ApiResult.success(userService.refreshToken(refreshToken));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestAttribute Long userId) {
        userService.logout(userId, null);
        return ApiResult.success();
    }
}
