package com.tutorlink.web.controller.order;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.order.CourseScheduleRequest;
import com.tutorlink.model.dto.order.MyScheduleResponse;
import com.tutorlink.model.entity.CourseSchedule;
import com.tutorlink.model.entity.LessonSession;
import com.tutorlink.service.order.CourseScheduleService;
import com.tutorlink.service.order.LessonSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程排期")
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class CourseScheduleController {

    private final CourseScheduleService scheduleService;
    private final LessonSessionService sessionService;

    @Operation(summary = "创建课程排期（家长或家教均可发起）")
    @PostMapping
    public ApiResult<List<CourseSchedule>> createSchedules(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CourseScheduleRequest request) {
        return ApiResult.success(scheduleService.createSchedules(userId, request));
    }

    @Operation(summary = "我的排期列表")
    @GetMapping("/my")
    public ApiResult<List<MyScheduleResponse>> listMySchedules(
            @AuthenticationPrincipal Long userId) {
        return ApiResult.success(scheduleService.listMySchedules(userId));
    }

    @Operation(summary = "确认排期")
    @PutMapping("/{id}/confirm")
    public ApiResult<Void> confirmSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        scheduleService.confirmSchedule(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "拒绝排期")
    @PutMapping("/{id}/reject")
    public ApiResult<Void> rejectSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        scheduleService.rejectSchedule(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "查询订单的排期")
    @GetMapping("/order/{orderId}")
    public ApiResult<List<CourseSchedule>> listByOrder(@PathVariable Long orderId) {
        return ApiResult.success(scheduleService.listByOrder(orderId));
    }

    @Operation(summary = "暂停排期")
    @PutMapping("/{id}/pause")
    public ApiResult<Void> pauseSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        scheduleService.pauseSchedule(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "恢复排期")
    @PutMapping("/{id}/resume")
    public ApiResult<Void> resumeSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        scheduleService.resumeSchedule(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "查询订单的课时列表")
    @GetMapping("/order/{orderId}/sessions")
    public ApiResult<List<LessonSession>> listSessionsByOrder(@PathVariable Long orderId) {
        return ApiResult.success(sessionService.listByOrder(orderId));
    }

    @Operation(summary = "老师签到")
    @PutMapping("/sessions/{sessionId}/check-in")
    public ApiResult<Void> checkIn(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Long userId) {
        sessionService.checkIn(sessionId, userId);
        return ApiResult.success();
    }

    @Operation(summary = "老师签退")
    @PutMapping("/sessions/{sessionId}/check-out")
    public ApiResult<Void> checkOut(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Long userId) {
        sessionService.checkOut(sessionId, userId);
        return ApiResult.success();
    }

    @Operation(summary = "家长确认课时")
    @PutMapping("/sessions/{sessionId}/confirm")
    public ApiResult<Void> parentConfirm(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Long userId) {
        sessionService.parentConfirm(sessionId, userId);
        return ApiResult.success();
    }
}
