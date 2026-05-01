package com.tutorlink.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.dao.mapper.*;
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

    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalTutors", tutorProfileMapper.selectCount(null));

        // 订单统计
        stats.put("totalOrders", orderMapper.selectCount(null));
        stats.put("pendingOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        stats.put("activeOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().in(Order::getStatus, 2, 3, 4)));
        stats.put("completedOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 5)));

        // 支付统计
        stats.put("totalPayments", paymentMapper.selectCount(null));

        // 评价统计
        stats.put("totalReviews", reviewMapper.selectCount(null));

        // 今日新增
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.put("todayNewUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));
        stats.put("todayNewOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, todayStart)));

        return stats;
    }
}
