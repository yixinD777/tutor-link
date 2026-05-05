package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.PaymentMapper;
import com.tutorlink.model.dto.admin.AdminPaymentQueryRequest;
import com.tutorlink.model.entity.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员 - 支付管理")
@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentMapper paymentMapper;

    @Operation(summary = "支付列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<IPage<Payment>> listPayments(AdminPaymentQueryRequest request) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (request.getStatus() != null) {
            wrapper.eq(Payment::getStatus, request.getStatus());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(Payment::getCreateTime, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(Payment::getCreateTime, request.getEndDate());
        }
        wrapper.orderByDesc(Payment::getCreateTime);
        IPage<Payment> page = paymentMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        return ApiResult.success(page);
    }

    @Operation(summary = "支付详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResult<Payment> getPaymentDetail(@PathVariable Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            return ApiResult.error(404, "支付记录不存在");
        }
        return ApiResult.success(payment);
    }
}
