package com.tutorlink.service.order;

import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.model.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderStateMachine orderStateMachine;

    public Order getOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }
}
