package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.UserMapper;
import com.tutorlink.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理员 - 用户管理")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    @Operation(summary = "禁用/启用用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateUserStatus(@PathVariable Long id,
                                             @RequestParam Integer status) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, status));
        return ApiResult.success();
    }
}
