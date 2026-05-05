package com.tutorlink.web.config;

import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.response.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResult<Void> handleAccessDenied(AccessDeniedException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        log.warn("Access denied: {}", e.getMessage());
        return ApiResult.error(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResult<Void> handleAuthentication(AuthenticationException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        log.warn("Authentication failed: {}", e.getMessage());
        return ApiResult.error(ResultCode.UNAUTHORIZED);
    }
}
