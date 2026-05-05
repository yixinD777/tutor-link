package com.tutorlink.web.controller.order;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.order.TrialLessonRequest;
import com.tutorlink.model.entity.TrialLesson;
import com.tutorlink.service.order.TrialLessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "试课管理")
@RestController
@RequestMapping("/api/v1/trials")
@RequiredArgsConstructor
public class TrialLessonController {

    private final TrialLessonService trialLessonService;

    @Operation(summary = "家长创建试课")
    @PostMapping
    public ApiResult<TrialLesson> createTrial(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TrialLessonRequest request) {
        return ApiResult.success(trialLessonService.createTrial(userId, request));
    }

    @Operation(summary = "老师确认试课")
    @PutMapping("/{id}/confirm")
    public ApiResult<Void> confirmTrial(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        trialLessonService.confirmTrial(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "家长评价试课")
    @PutMapping("/{id}/evaluate")
    public ApiResult<Void> evaluateTrial(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, Object> body) {
        Integer result = (Integer) body.get("result");
        String feedback = (String) body.get("feedback");
        trialLessonService.evaluateTrial(id, userId, result, feedback);
        return ApiResult.success();
    }

    @Operation(summary = "查询订单的试课记录")
    @GetMapping("/order/{orderId}")
    public ApiResult<List<TrialLesson>> listByOrder(@PathVariable Long orderId) {
        return ApiResult.success(trialLessonService.listByOrder(orderId));
    }

    @Operation(summary = "试课详情")
    @GetMapping("/{id}")
    public ApiResult<TrialLesson> getById(@PathVariable Long id) {
        return ApiResult.success(trialLessonService.getById(id));
    }
}
