package com.tutorlink.web.controller.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.order.OrderCancelRequest;
import com.tutorlink.model.dto.order.OrderCreateRequest;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.OrderLog;
import com.tutorlink.service.order.OrderQueryService;
import com.tutorlink.service.order.OrderService;
import com.tutorlink.service.order.OrderStateMachine;
import com.tutorlink.service.order.OrderExpireScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderStateMachine orderStateMachine;
    private final OrderQueryService orderQueryService;
    private final OrderExpireScheduler orderExpireScheduler;

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
            @RequestParam(defaultValue = "0") int role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(orderQueryService.listMyOrders(userId, role, status, page, size));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public ApiResult<Order> getOrder(@PathVariable Long id,
                                      @AuthenticationPrincipal Long userId,
                                      @RequestParam(defaultValue = "0") int role) {
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
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer teachingMode,
            @RequestParam(required = false) Integer hourlyRateMin,
            @RequestParam(required = false) Integer hourlyRateMax,
            @RequestParam(required = false) java.math.BigDecimal lng,
            @RequestParam(required = false) java.math.BigDecimal lat,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(orderQueryService.listPendingOrders(
                subjectId, teachingMode, hourlyRateMin, hourlyRateMax,
                lng, lat, radiusKm, page, size));
    }

    @Operation(summary = "接单 (家教)")
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/{id}/accept")
    public ApiResult<Void> acceptOrder(@PathVariable Long id,
                                        @AuthenticationPrincipal Long userId) {
        orderStateMachine.acceptOrder(id, userId);
        // 接单后添加 30 分钟未支付自动取消的延迟任务
        orderExpireScheduler.addExpireTask(id);
        return ApiResult.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancelOrder(@PathVariable Long id,
                                        @AuthenticationPrincipal Long userId,
                                        @RequestParam(defaultValue = "0") int role,
                                        @RequestBody(required = false) OrderCancelRequest request) {
        orderStateMachine.cancelOrder(id, userId, role, request);
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
}
