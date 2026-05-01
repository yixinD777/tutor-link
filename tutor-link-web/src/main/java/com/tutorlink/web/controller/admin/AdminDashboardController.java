package com.tutorlink.web.controller.admin;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.service.admin.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "管理员 - 仪表盘")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> getStats() {
        return ApiResult.success(dashboardService.getStats());
    }
}
