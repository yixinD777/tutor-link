package com.tutorlink.web.controller.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorlink.common.constant.CertificationStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.model.dto.order.OrderCancelRequest;
import com.tutorlink.model.dto.order.OrderCreateRequest;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.OrderLog;
import com.tutorlink.model.entity.TutorProfile;
import com.tutorlink.service.order.OrderQueryService;
import com.tutorlink.service.order.OrderService;
import com.tutorlink.service.order.OrderStateMachine;
import com.tutorlink.service.order.OrderExpireScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理")
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderStateMachine orderStateMachine;
    private final OrderQueryService orderQueryService;
    private final OrderExpireScheduler orderExpireScheduler;
    private final TutorProfileMapper tutorProfileMapper;

    @Operation(summary = "创建订单 (家长)")
    @PreAuthorize("hasRole('PARENT')")
    @PostMapping
    public ApiResult<Order> createOrder(@Valid @RequestBody OrderCreateRequest request,
                                         @AuthenticationPrincipal Long userId) {
        return ApiResult.success(orderStateMachine.createOrder(request, userId));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping
    public ApiResult<IPage<Order>> listMyOrders(
            @AuthenticationPrincipal Long userId,
            Authentication authentication,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(orderQueryService.listMyOrders(userId, getRole(authentication), status, page, size));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public ApiResult<Order> getOrder(@PathVariable Long id,
                                      @AuthenticationPrincipal Long userId,
                                      Authentication authentication) {
        int role = getRole(authentication);
        log.debug("[OrderController] getOrder id={} userId={} role={}", id, userId, role);
        return ApiResult.success(orderQueryService.getOrderDetail(id, userId, role));
    }

    @Operation(summary = "订单状态流转日志")
    @GetMapping("/{id}/logs")
    public ApiResult<List<OrderLog>> getOrderLogs(@PathVariable Long id) {
        return ApiResult.success(orderQueryService.getOrderLogs(id));
    }

    @Operation(summary = "待接单订单 (家教)")
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/pending")
    public ApiResult<IPage<Order>> listPendingOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer teachingMode,
            @RequestParam(required = false) Integer hourlyRateMin,
            @RequestParam(required = false) Integer hourlyRateMax,
            @RequestParam(required = false) java.math.BigDecimal lng,
            @RequestParam(required = false) java.math.BigDecimal lat,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        checkCertification(userId);
        return ApiResult.success(orderQueryService.listPendingOrders(
                subjectId, teachingMode, hourlyRateMin, hourlyRateMax,
                lng, lat, radiusKm, page, size));
    }

    @Operation(summary = "待接单订单 Feed 流 (游标分页)")
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/pending/feed")
    public ApiResult<com.tutorlink.model.dto.common.CursorPageResponse<Order>> listPendingOrdersFeed(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer teachingMode,
            @RequestParam(required = false) Integer hourlyRateMin,
            @RequestParam(required = false) Integer hourlyRateMax) {
        checkCertification(userId);
        return ApiResult.success(orderQueryService.listPendingOrdersFeed(
                cursor, limit, subjectId, teachingMode, hourlyRateMin, hourlyRateMax));
    }

    @Operation(summary = "表达意向 (家教)")
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/{id}/interest")
    public ApiResult<Void> expressInterest(@PathVariable Long id,
                                            @AuthenticationPrincipal Long userId) {
        checkCertification(userId);
        orderStateMachine.expressInterest(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "确认委托 (家长)")
    @PreAuthorize("hasRole('PARENT')")
    @PutMapping("/{id}/confirm-delegation")
    public ApiResult<Void> confirmDelegation(@PathVariable Long id,
                                              @AuthenticationPrincipal Long userId) {
        orderStateMachine.confirmDelegation(id, userId);
        // 确认委托后添加 30 分钟未支付自动取消的延迟任务
        orderExpireScheduler.addExpireTask(id);
        return ApiResult.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancelOrder(@PathVariable Long id,
                                        @AuthenticationPrincipal Long userId,
                                        Authentication authentication,
                                        @RequestBody(required = false) OrderCancelRequest request) {
        orderStateMachine.cancelOrder(id, userId, getRole(authentication), request);
        orderExpireScheduler.removeExpireTask(id);
        return ApiResult.success();
    }

    @Operation(summary = "开始上课 (家教)")
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/{id}/start")
    public ApiResult<Void> startOrder(@PathVariable Long id,
                                       @AuthenticationPrincipal Long userId) {
        orderStateMachine.startOrder(id, userId);
        return ApiResult.success();
    }

    @Operation(summary = "确认完成 (家长)")
    @PreAuthorize("hasRole('PARENT')")
    @PutMapping("/{id}/complete")
    public ApiResult<Void> completeOrder(@PathVariable Long id,
                                          @AuthenticationPrincipal Long userId) {
        orderStateMachine.completeOrder(id, userId);
        return ApiResult.success();
    }

    /**
     * 校验家教认证状态：未通过认证不允许查看待接单/接单
     */
    private void checkCertification(Long userId) {
        TutorProfile profile = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, userId));
        if (profile == null || profile.getCertificationStatus() != CertificationStatus.APPROVED.getCode()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请先完成学生认证后再接单");
        }
    }

    /**
     * 从 JWT Authentication 中提取用户角色
     */
    private int getRole(Authentication authentication) {
        Object details = authentication.getDetails();
        return details instanceof Integer ? (Integer) details : 0;
    }
}
