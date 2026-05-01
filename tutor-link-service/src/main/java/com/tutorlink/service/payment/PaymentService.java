package com.tutorlink.service.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.util.SnowflakeIdUtil;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.dao.mapper.PaymentMapper;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Value("${platform.fee-rate:0.05}")
    private double platformFeeRate;

    /**
     * 创建支付记录 (订单确认后调用)
     */
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 检查是否已有支付记录
        Payment existing = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getOrderId, orderId)
                        .ne(Payment::getStatus, 4)); // 排除已冻结的
        if (existing != null) {
            return existing;
        }

        int totalAmount = order.getTotalAmount();
        int platformFee = (int) (totalAmount * platformFeeRate);
        int tutorAmount = totalAmount - platformFee;

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(orderId);
        payment.setPayerUserId(order.getParentUserId());
        payment.setPayeeUserId(order.getTutorUserId());
        payment.setAmount(totalAmount);
        payment.setPlatformFee(platformFee);
        payment.setTutorAmount(tutorAmount);
        payment.setPayChannel(1); // 微信小程序支付
        payment.setStatus(1); // 待支付
        paymentMapper.insert(payment);

        return payment;
    }

    /**
     * 微信支付回调处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePayCallback(String paymentNo, String wxTransactionId) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPaymentNo, paymentNo));
        if (payment == null) {
            log.error("Payment not found for paymentNo: {}", paymentNo);
            return;
        }

        if (payment.getStatus() != 1) {
            log.warn("Payment already processed: paymentNo={}, status={}", paymentNo, payment.getStatus());
            return;
        }

        payment.setStatus(2); // 已支付
        payment.setWxTransactionId(wxTransactionId);
        payment.setPaidTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        // 更新订单状态为已支付
        Order order = orderMapper.selectById(payment.getOrderId());
        if (order != null && order.getStatus() == OrderStatus.CONFIRMED.getCode()) {
            order.setStatus(OrderStatus.PAID.getCode());
            orderMapper.updateById(order);
        }
    }

    /**
     * 放款给家教 (订单完成后调用)
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseToTutor(Long orderId) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getOrderId, orderId)
                        .eq(Payment::getStatus, 2)); // 已支付
        if (payment == null) {
            log.error("No paid payment found for orderId: {}", orderId);
            return;
        }

        payment.setStatus(3); // 已放款
        payment.setReleasedTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        log.info("Released payment to tutor: orderId={}, tutorUserId={}, amount={}",
                orderId, payment.getPayeeUserId(), payment.getTutorAmount());
        // TODO: 调用微信付款到家教零钱 (企业付款/批量转账)
    }

    /**
     * 获取订单的支付记录
     */
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getOrderId, orderId)
                        .orderByDesc(Payment::getCreateTime)
                        .last("LIMIT 1"));
    }

    private String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "PAY" + timestamp + random;
    }
}
