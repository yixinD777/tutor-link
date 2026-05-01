package com.tutorlink.service.payment;

import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.dao.mapper.RefundMapper;
import com.tutorlink.model.entity.Payment;
import com.tutorlink.model.entity.Refund;
import com.tutorlink.common.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundMapper refundMapper;
    private final PaymentService paymentService;

    /**
     * 发起退款
     */
    @Transactional(rollbackFor = Exception.class)
    public Refund createRefund(Long orderId, Long applicantId, String reason) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null || payment.getStatus() != 2) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND, "未找到可退款的支付记录");
        }

        // 检查是否已有退款记录
        Long existingCount = refundMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Refund>()
                        .eq(Refund::getOrderId, orderId)
                        .in(Refund::getStatus, 1, 2)); // 待审核或退款中
        if (existingCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已有退款申请在处理中");
        }

        Refund refund = new Refund();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(orderId);
        refund.setPaymentId(payment.getId());
        refund.setApplicantId(applicantId);
        refund.setAmount(payment.getAmount());
        refund.setReason(reason);
        refund.setStatus(1); // 待审核
        refundMapper.insert(refund);

        return refund;
    }

    /**
     * 处理退款回调
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundCallback(String refundNo, String wxRefundId, boolean success) {
        Refund refund = refundMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Refund>()
                        .eq(Refund::getRefundNo, refundNo));
        if (refund == null) {
            log.error("Refund not found: refundNo={}", refundNo);
            return;
        }

        refund.setWxRefundId(wxRefundId);
        if (success) {
            refund.setStatus(3); // 退款成功
            refund.setCompleteTime(LocalDateTime.now());
        } else {
            refund.setStatus(4); // 退款失败
        }
        refundMapper.updateById(refund);
    }

    private String generateRefundNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "RFD" + timestamp + random;
    }
}
