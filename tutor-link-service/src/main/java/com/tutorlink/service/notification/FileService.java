package com.tutorlink.service.notification;

import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传服务
 * 开发环境使用本地存储，生产环境切换为阿里云 OSS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 JPG/PNG/WebP 格式");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, filePath);
            }

            return baseUrl + "/" + fileName;
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }
    }
}
