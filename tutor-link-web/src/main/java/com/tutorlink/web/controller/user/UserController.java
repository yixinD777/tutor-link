package com.tutorlink.web.controller.user;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.entity.User;
import com.tutorlink.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ApiResult<User> getCurrentUser(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(userService.getUserById(userId));
    }
}
