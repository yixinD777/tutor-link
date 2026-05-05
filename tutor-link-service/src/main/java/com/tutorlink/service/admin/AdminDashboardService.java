package com.tutorlink.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.dao.mapper.*;
import com.tutorlink.model.dto.admin.DashboardStatsResponse;
import com.tutorlink.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final TutorProfileMapper tutorProfileMapper;
    private final PaymentMapper paymentMapper;
    private final ReviewMapper reviewMapper;
    private final StudentCertificationMapper certificationMapper;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalTutors", tutorProfileMapper.selectCount(null));
        stats.put("totalOrders", orderMapper.selectCount(null));
        stats.put("pendingOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        stats.put("activeOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().in(Order::getStatus, 2, 3, 4)));
        stats.put("completedOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 5)));
        stats.put("totalPayments", paymentMapper.selectCount(null));
        stats.put("totalReviews", reviewMapper.selectCount(null));
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.put("todayNewUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));
        stats.put("todayNewOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, todayStart)));
        return stats;
    }

    public DashboardStatsResponse getDetailedStats() {
        DashboardStatsResponse resp = new DashboardStatsResponse();
        resp.setTotalUsers(userMapper.selectCount(null));
        resp.setTotalTutors(tutorProfileMapper.selectCount(null));
        resp.setTotalOrders(orderMapper.selectCount(null));
        resp.setActiveOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().in(Order::getStatus, 2, 3, 4)));
        resp.setCompletedOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 5)));
        resp.setTotalPayments(paymentMapper.selectCount(null));
        resp.setTotalReviews(reviewMapper.selectCount(null));

        // pending certifications
        resp.setPendingCertifications(certificationMapper.selectCount(
                new LambdaQueryWrapper<StudentCertification>().eq(StudentCertification::getStatus, 1)));

        // today stats
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        resp.setTodayNewUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));
        resp.setTodayNewOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, todayStart)));

        // revenue (sum of paid/released payments)
        resp.setTotalRevenue(paymentMapper.selectCount(null) > 0 ? 0L : 0L);
        // Use a simple approach - sum all paid amounts
        var paidPayments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>().ge(Payment::getStatus, 2));
        long totalRevenue = paidPayments.stream().mapToLong(p -> p.getAmount() != null ? p.getAmount() : 0).sum();
        resp.setTotalRevenue(totalRevenue);

        // today revenue
        var todayPayments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>().ge(Payment::getStatus, 2).ge(Payment::getPaidTime, todayStart));
        long todayRevenue = todayPayments.stream().mapToLong(p -> p.getAmount() != null ? p.getAmount() : 0).sum();
        resp.setTodayRevenue(todayRevenue);

        return resp;
    }
}
