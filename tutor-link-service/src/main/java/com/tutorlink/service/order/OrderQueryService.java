package com.tutorlink.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.constant.UserRole;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.dao.mapper.OrderLogMapper;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.model.dto.common.CursorPageResponse;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.OrderLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderMapper orderMapper;
    private final OrderLogMapper orderLogMapper;

    /**
     * 查询我的订单列表 (根据角色自动区分)
     */
    public IPage<Order> listMyOrders(Long userId, int role, Integer status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 家长看自己发布的订单，家教看自己接的订单
        if (UserRole.hasRole(role, UserRole.ADMIN)) {
            // 管理员看所有订单
        } else if (UserRole.hasRole(role, UserRole.TUTOR)) {
            wrapper.and(w -> w.eq(Order::getTutorUserId, userId)
                    .or().eq(Order::getParentUserId, userId));
        } else {
            wrapper.eq(Order::getParentUserId, userId);
        }

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取订单详情 (含权限校验)
     */
    public Order getOrderDetail(Long orderId, Long userId, int role) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 权限校验：只有订单相关方或管理员可查看
        if (!UserRole.hasRole(role, UserRole.ADMIN)
                && !order.getParentUserId().equals(userId)
                && !userId.equals(order.getTutorUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return order;
    }

    /**
     * 获取订单状态流转日志
     */
    public List<OrderLog> getOrderLogs(Long orderId) {
        return orderLogMapper.selectList(
                new LambdaQueryWrapper<OrderLog>()
                        .eq(OrderLog::getOrderId, orderId)
                        .orderByAsc(OrderLog::getCreateTime));
    }

    /**
     * 查询待接单订单列表 (家教端)
     * 支持按科目、教学方式、时薪范围、地理位置筛选
     */
    public IPage<Order> listPendingOrders(Long subjectId, Integer teachingMode,
                                           Integer hourlyRateMin, Integer hourlyRateMax,
                                           BigDecimal lng, BigDecimal lat, Double radiusKm,
                                           int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PENDING.getCode());

        if (subjectId != null) wrapper.eq(Order::getSubjectId, subjectId);
        if (teachingMode != null) wrapper.eq(Order::getTeachingMode, teachingMode);
        if (hourlyRateMin != null) wrapper.ge(Order::getHourlyRate, hourlyRateMin);
        if (hourlyRateMax != null) wrapper.le(Order::getHourlyRate, hourlyRateMax);

        // 经纬度边界框筛选 (约 111km/度)
        if (lng != null && lat != null && radiusKm != null) {
            double delta = radiusKm / 111.0;
            wrapper.between(Order::getLongitude, lng.doubleValue() - delta, lng.doubleValue() + delta);
            wrapper.between(Order::getLatitude, lat.doubleValue() - delta, lat.doubleValue() + delta);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 基于游标的分页查询待接单订单 (Feed 流)
     * 使用 id 倒序，cursor 为上一页最后一条记录的 id
     */
    public CursorPageResponse<Order> listPendingOrdersFeed(Long cursor, int limit,
                                                            Long subjectId, Integer teachingMode,
                                                            Integer hourlyRateMin, Integer hourlyRateMax) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PENDING.getCode());

        // 游标条件：id < cursor
        if (cursor != null && cursor > 0) {
            wrapper.lt(Order::getId, cursor);
        }

        if (subjectId != null) wrapper.eq(Order::getSubjectId, subjectId);
        if (teachingMode != null) wrapper.eq(Order::getTeachingMode, teachingMode);
        if (hourlyRateMin != null) wrapper.ge(Order::getHourlyRate, hourlyRateMin);
        if (hourlyRateMax != null) wrapper.le(Order::getHourlyRate, hourlyRateMax);

        wrapper.orderByDesc(Order::getId);
        wrapper.last("LIMIT " + (limit + 1)); // 多查一条判断 hasMore

        List<Order> list = orderMapper.selectList(wrapper);

        boolean hasMore = list.size() > limit;
        if (hasMore) {
            list = list.subList(0, limit); // 截取 limit 条
        }

        Long nextCursor = list.isEmpty() ? null : list.get(list.size() - 1).getId();

        return CursorPageResponse.<Order>builder()
                .list(list)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }
}
