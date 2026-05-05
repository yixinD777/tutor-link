package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.RefundMapper;
import com.tutorlink.model.dto.admin.AdminRefundQueryRequest;
import com.tutorlink.model.entity.Refund;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员 - 退款管理")
@RestController
@RequestMapping("/api/v1/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundMapper refundMapper;

    @Operation(summary = "退款列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<IPage<Refund>> listRefunds(AdminRefundQueryRequest request) {
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        if (request.getStatus() != null) {
            wrapper.eq(Refund::getStatus, request.getStatus());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(Refund::getCreateTime, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(Refund::getCreateTime, request.getEndDate());
        }
        wrapper.orderByDesc(Refund::getCreateTime);
        IPage<Refund> page = refundMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        return ApiResult.success(page);
    }

    @Operation(summary = "退款详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResult<Refund> getRefundDetail(@PathVariable Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            return ApiResult.error(404, "退款记录不存在");
        }
        return ApiResult.success(refund);
    }
}
