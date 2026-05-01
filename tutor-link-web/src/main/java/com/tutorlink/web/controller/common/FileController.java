package com.tutorlink.web.controller.common;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.service.notification.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public ApiResult<String> uploadFile(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {
        return ApiResult.success(fileService.uploadFile(file));
    }
}
