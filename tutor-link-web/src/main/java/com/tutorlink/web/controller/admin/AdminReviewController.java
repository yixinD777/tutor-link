package com.tutorlink.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.ReviewMapper;
import com.tutorlink.model.dto.admin.AdminReviewQueryRequest;
import com.tutorlink.model.entity.Review;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员 - 评价管理")
@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewMapper reviewMapper;

    @Operation(summary = "评价列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<IPage<Review>> listReviews(AdminReviewQueryRequest request) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (request.getRating() != null) {
            wrapper.eq(Review::getRating, request.getRating());
        }
        if (request.getReviewerRole() != null) {
            wrapper.eq(Review::getReviewerRole, request.getReviewerRole());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(Review::getCreateTime, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            wrapper.le(Review::getCreateTime, request.getEndDate());
        }
        wrapper.orderByDesc(Review::getCreateTime);
        IPage<Review> page = reviewMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        return ApiResult.success(page);
    }

    @Operation(summary = "评价详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResult<Review> getReviewDetail(@PathVariable Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return ApiResult.error(404, "评价不存在");
        }
        return ApiResult.success(review);
    }
}
