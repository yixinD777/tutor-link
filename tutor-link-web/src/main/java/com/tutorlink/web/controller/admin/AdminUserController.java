package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.dao.mapper.UserMapper;
import com.tutorlink.dao.mapper.UserProfileMapper;
import com.tutorlink.model.dto.admin.AdminUserQueryRequest;
import com.tutorlink.model.entity.TutorProfile;
import com.tutorlink.model.entity.User;
import com.tutorlink.model.entity.UserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "管理员 - 用户管理")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final TutorProfileMapper tutorProfileMapper;

    @Operation(summary = "用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<IPage<User>> listUsers(AdminUserQueryRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (request.getRole() != null) {
            wrapper.eq(User::getRole, request.getRole());
        }
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(User::getAccount, request.getKeyword())
                    .or().like(User::getNickname, request.getKeyword())
                    .or().like(User::getPhone, request.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreateTime);
        // Hide password field
        wrapper.select(User.class, info -> !info.getColumn().equals("password"));
        IPage<User> page = userMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        return ApiResult.success(page);
    }

    @Operation(summary = "用户详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResult.error(404, "用户不存在");
        }
        user.setPassword(null);

        Map<String, Object> detail = new HashMap<>();
        detail.put("user", user);

        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, id));
        detail.put("profile", profile);

        if (user.getRole() != null && (user.getRole() & 2) != 0) {
            TutorProfile tutorProfile = tutorProfileMapper.selectOne(
                    new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, id));
            detail.put("tutorProfile", tutorProfile);
        }

        return ApiResult.success(detail);
    }

    @Operation(summary = "禁用/启用用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateUserStatus(@PathVariable Long id,
                                             @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return ApiResult.success();
    }
}
