package com.tutorlink.web.controller.user;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.entity.StudentCertification;
import com.tutorlink.service.user.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生认证")
@RestController
@RequestMapping("/api/v1/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    @Operation(summary = "提交学生认证")
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/apply")
    public ApiResult<StudentCertification> applyCertification(
            @AuthenticationPrincipal Long userId,
            @RequestParam String realName,
            @RequestParam(required = false) String idCardNo,
            @RequestParam(required = false) String studentIdNo,
            @RequestParam String photoFront,
            @RequestParam(required = false) String photoBack,
            @RequestParam(required = false) String handheldPhoto,
            @RequestParam(required = false) String ocrResult) {
        return ApiResult.success(certificationService.applyCertification(
                userId, realName, idCardNo, studentIdNo,
                photoFront, photoBack, handheldPhoto, ocrResult));
    }

    @Operation(summary = "获取我的认证状态")
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/me")
    public ApiResult<StudentCertification> getMyCertification(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(certificationService.getMyCertification(userId));
    }
}
