package com.tutorlink.web.controller.admin;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.admin.*;
import com.tutorlink.service.admin.AdminAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员 - 数据分析")
@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    @Operation(summary = "用户增长趋势")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user-trend")
    public ApiResult<TrendDataResponse> getUserTrend(TrendQueryRequest request) {
        return ApiResult.success(analyticsService.getUserTrend(request));
    }

    @Operation(summary = "订单趋势")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order-trend")
    public ApiResult<TrendDataResponse> getOrderTrend(TrendQueryRequest request) {
        return ApiResult.success(analyticsService.getOrderTrend(request));
    }

    @Operation(summary = "收入趋势")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue-trend")
    public ApiResult<TrendDataResponse> getRevenueTrend(TrendQueryRequest request) {
        return ApiResult.success(analyticsService.getRevenueTrend(request));
    }

    @Operation(summary = "退款趋势")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/refund-trend")
    public ApiResult<TrendDataResponse> getRefundTrend(TrendQueryRequest request) {
        return ApiResult.success(analyticsService.getRefundTrend(request));
    }

    @Operation(summary = "评价评分趋势")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/review-rating-trend")
    public ApiResult<TrendDataResponse> getReviewRatingTrend(TrendQueryRequest request) {
        return ApiResult.success(analyticsService.getReviewRatingTrend(request));
    }

    @Operation(summary = "订单状态分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order-status-distribution")
    public ApiResult<DistributionDataResponse> getOrderStatusDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getOrderStatusDistribution(request));
    }

    @Operation(summary = "科目热度分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/subject-popularity")
    public ApiResult<DistributionDataResponse> getSubjectPopularity(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getSubjectPopularity(request));
    }

    @Operation(summary = "教学模式分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/teaching-mode-distribution")
    public ApiResult<DistributionDataResponse> getTeachingModeDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getTeachingModeDistribution(request));
    }

    @Operation(summary = "性别分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/gender-distribution")
    public ApiResult<DistributionDataResponse> getGenderDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getGenderDistribution(request));
    }

    @Operation(summary = "地区分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/region-distribution")
    public ApiResult<DistributionDataResponse> getRegionDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getRegionDistribution(request));
    }

    @Operation(summary = "家教评分分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tutor-rating-distribution")
    public ApiResult<DistributionDataResponse> getTutorRatingDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getTutorRatingDistribution(request));
    }

    @Operation(summary = "大学分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/university-distribution")
    public ApiResult<DistributionDataResponse> getUniversityDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getUniversityDistribution(request));
    }

    @Operation(summary = "认证状态分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/certification-status-distribution")
    public ApiResult<DistributionDataResponse> getCertificationStatusDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getCertificationStatusDistribution(request));
    }

    @Operation(summary = "时薪分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/hourly-rate-distribution")
    public ApiResult<DistributionDataResponse> getHourlyRateDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getHourlyRateDistribution(request));
    }

    @Operation(summary = "支付状态分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payment-status-distribution")
    public ApiResult<DistributionDataResponse> getPaymentStatusDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getPaymentStatusDistribution(request));
    }

    @Operation(summary = "评价评分分布")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/review-rating-distribution")
    public ApiResult<DistributionDataResponse> getReviewRatingDistribution(DistributionQueryRequest request) {
        return ApiResult.success(analyticsService.getReviewRatingDistribution(request));
    }
}
