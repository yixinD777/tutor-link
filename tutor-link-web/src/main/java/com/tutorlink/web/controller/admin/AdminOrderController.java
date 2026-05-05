package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.OrderLogMapper;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.dao.mapper.PaymentMapper;
import com.tutorlink.model.dto.admin.AdminOrderQueryRequest;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.OrderLog;
import com.tutorlink.model.entity.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理员 - 订单管理")
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderMapper orderMapper;
    private final OrderLogMapper orderLogMapper;
    private final PaymentMapper paymentMapper;

    @Operation(summary = "订单列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<IPage<Order>> listOrders(AdminOrderQueryRequest request) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (request.getStatus() != null) {
            wrapper.eq(Order::getStatus, request.getStatus());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(Order::getCreateTime, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(Order::getCreateTime, request.getEndDate());
        }
        if (request.getMinAmount() != null) {
            wrapper.ge(Order::getTotalAmount, request.getMinAmount());
        }
        if (request.getMaxAmount() != null) {
            wrapper.le(Order::getTotalAmount, request.getMaxAmount());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Order::getOrderNo, request.getKeyword())
                    .or().like(Order::getTitle, request.getKeyword()));
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> page = orderMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        return ApiResult.success(page);
    }

    @Operation(summary = "订单详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return ApiResult.error(404, "订单不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("order", order);

        // order logs
        List<OrderLog> logs = orderLogMapper.selectList(
                new LambdaQueryWrapper<OrderLog>()
                        .eq(OrderLog::getOrderId, id)
                        .orderByAsc(OrderLog::getCreateTime));
        detail.put("logs", logs);

        // payment info
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, id));
        detail.put("payment", payment);

        return ApiResult.success(detail);
    }
}
