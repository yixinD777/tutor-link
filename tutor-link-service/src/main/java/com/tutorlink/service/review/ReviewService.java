package com.tutorlink.service.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.constant.UserRole;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.util.SnowflakeIdUtil;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.dao.mapper.ReviewMapper;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.model.dto.review.ReviewCreateRequest;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.Review;
import com.tutorlink.model.entity.TutorProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final TutorProfileMapper tutorProfileMapper;

    /**
     * 创建评价
     */
    @Transactional
    public Review createReview(ReviewCreateRequest request, Long reviewerId, int role) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderStatus.COMPLETED.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }

        // 判断评价对象
        Long revieweeId;
        int reviewerRole;
        if (UserRole.hasRole(role, UserRole.PARENT) && order.getParentUserId().equals(reviewerId)) {
            // 家长评价家教
            revieweeId = order.getTutorUserId();
            reviewerRole = 1;
        } else if (UserRole.hasRole(role, UserRole.TUTOR) && order.getTutorUserId().equals(reviewerId)) {
            // 家教评价家长
            revieweeId = order.getParentUserId();
            reviewerRole = 2;
        } else {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 检查是否已评价
        Long existCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, request.getOrderId())
                        .eq(Review::getReviewerId, reviewerId));
        if (existCount > 0) {
            throw new BusinessException(ResultCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review();
        review.setId(SnowflakeIdUtil.nextId());
        review.setOrderId(request.getOrderId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setReviewerRole(reviewerRole);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setTags(request.getTags());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : 0);
        reviewMapper.insert(review);

        // 家教被评价时，更新家教评分
        if (reviewerRole == 1) {
            updateTutorRating(revieweeId);
        }

        log.info("Review created: orderId={}, reviewer={}, reviewee={}", request.getOrderId(), reviewerId, revieweeId);
        return review;
    }

    /**
     * 获取家教的评价列表
     */
    public List<Review> listTutorReviews(Long tutorUserId) {
        return reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getRevieweeId, tutorUserId)
                        .eq(Review::getReviewerRole, 1)
                        .orderByDesc(Review::getCreateTime));
    }

    /**
     * 获取订单的评价
     */
    public List<Review> getOrderReviews(Long orderId) {
        return reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, orderId)
                        .orderByAsc(Review::getCreateTime));
    }

    /**
     * 更新家教平均评分 (加权平均)
     */
    private void updateTutorRating(Long tutorUserId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getRevieweeId, tutorUserId)
                        .eq(Review::getReviewerRole, 1));

        if (reviews.isEmpty()) return;

        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        java.math.BigDecimal avgRating = java.math.BigDecimal.valueOf(avg).setScale(1, java.math.RoundingMode.HALF_UP);

        tutorProfileMapper.update(null,
                new LambdaUpdateWrapper<TutorProfile>()
                        .eq(TutorProfile::getUserId, tutorUserId)
                        .set(TutorProfile::getAvgRating, avgRating)
                        .setSql("rating_count = rating_count + 1"));

        log.info("Tutor rating updated: userId={}, avg={}", tutorUserId, avg);
    }
}
