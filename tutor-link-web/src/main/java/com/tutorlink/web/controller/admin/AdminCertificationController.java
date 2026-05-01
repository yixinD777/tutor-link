package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.entity.StudentCertification;
import com.tutorlink.service.user.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员 - 认证审核")
@RestController
@RequestMapping("/api/v1/admin/certifications")
@RequiredArgsConstructor
public class AdminCertificationController {

    private final CertificationService certificationService;

    @Operation(summary = "获取待审核认证列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<IPage<StudentCertification>> listPendingCertifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(certificationService.listPendingCertifications(page, size));
    }

    @Operation(summary = "审核通过")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ApiResult<Void> approveCertification(
            @PathVariable Long id,
            @AuthenticationPrincipal Long reviewerId) {
        certificationService.approveCertification(id, reviewerId);
        return ApiResult.success();
    }

    @Operation(summary = "审核拒绝")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ApiResult<Void> rejectCertification(
            @PathVariable Long id,
            @AuthenticationPrincipal Long reviewerId,
            @RequestParam String reason) {
        certificationService.rejectCertification(id, reviewerId, reason);
        return ApiResult.success();
    }
}
