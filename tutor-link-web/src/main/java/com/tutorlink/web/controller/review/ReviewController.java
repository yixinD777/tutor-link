package com.tutorlink.web.controller.review;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.review.ReviewCreateRequest;
import com.tutorlink.model.entity.Review;
import com.tutorlink.service.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评价")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "创建评价")
    @PostMapping
    public ApiResult<Review> createReview(@Valid @RequestBody ReviewCreateRequest request,
                                           @AuthenticationPrincipal Long userId,
                                           @RequestParam(defaultValue = "0") int role) {
        return ApiResult.success(reviewService.createReview(request, userId, role));
    }

    @Operation(summary = "获取家教评价列表")
    @GetMapping("/tutor/{userId}")
    public ApiResult<List<Review>> listTutorReviews(@PathVariable Long userId) {
        return ApiResult.success(reviewService.listTutorReviews(userId));
    }

    @Operation(summary = "获取订单评价")
    @GetMapping("/order/{orderId}")
    public ApiResult<List<Review>> getOrderReviews(@PathVariable Long orderId) {
        return ApiResult.success(reviewService.getOrderReviews(orderId));
    }
}
