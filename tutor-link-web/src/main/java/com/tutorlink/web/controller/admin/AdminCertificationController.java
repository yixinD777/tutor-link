package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.constant.CertificationStatus;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.StudentCertificationMapper;
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
    private final StudentCertificationMapper certificationMapper;

    @Operation(summary = "获取认证列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<IPage<StudentCertification>> listCertifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        if (status != null) {
            LambdaQueryWrapper<StudentCertification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentCertification::getStatus, status);
            wrapper.orderByDesc(StudentCertification::getCreateTime);
            return ApiResult.success(certificationMapper.selectPage(new Page<>(page, size), wrapper));
        }
        return ApiResult.success(certificationService.listPendingCertifications(page, size));
    }

    @Operation(summary = "认证详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResult<StudentCertification> getCertificationDetail(@PathVariable Long id) {
        StudentCertification cert = certificationMapper.selectById(id);
        if (cert == null) {
            return ApiResult.error(404, "认证记录不存在");
        }
        return ApiResult.success(cert);
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
