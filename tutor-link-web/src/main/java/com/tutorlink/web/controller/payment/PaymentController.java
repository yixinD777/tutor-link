package com.tutorlink.web.controller.payment;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.entity.Payment;
import com.tutorlink.service.payment.PaymentService;
import com.tutorlink.service.payment.WechatPayService;
import com.tutorlink.service.payment.RefundService;
import com.tutorlink.model.entity.Refund;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "支付管理")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final WechatPayService wechatPayService;
    private final RefundService refundService;

    @Operation(summary = "创建预付单 (家长支付) - 开发环境自动支付成功")
    @PreAuthorize("hasRole('PARENT')")
    @PostMapping("/prepay")
    public ApiResult<Payment> createPrepay(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long orderId) {
        Payment payment = paymentService.createPayment(orderId);

        // 开发环境：跳过微信支付，直接模拟支付成功
        paymentService.handlePayCallback(payment.getPaymentNo(), "DEV_MOCK_" + payment.getPaymentNo());

        return ApiResult.success(payment);
    }

    @Operation(summary = "微信支付回调")
    @PostMapping("/wechat-callback")
    public Map<String, String> wechatCallback(@RequestBody String body,
                                               @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
                                               @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
                                               @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce) {
        log.info("WeChat pay callback received");

        // TODO: 验证签名
        // if (!wechatPayService.verifyCallbackSignature(body, signature, timestamp, nonce)) {
        //     return Map.of("code", "FAIL", "message", "签名验证失败");
        // }

        // TODO: 解密回调数据，提取 out_trade_no 和 transaction_id
        // paymentService.handlePayCallback(outTradeNo, transactionId);

        return Map.of("code", "SUCCESS", "message", "成功");
    }

    @Operation(summary = "微信退款回调")
    @PostMapping("/refund-callback")
    public Map<String, String> refundCallback(@RequestBody String body) {
        log.info("WeChat refund callback received");
        // TODO: 验签 + 解密 + 处理退款结果
        return Map.of("code", "SUCCESS", "message", "成功");
    }

    @Operation(summary = "申请退款")
    @PostMapping("/{orderId}/refund")
    public ApiResult<Refund> createRefund(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long orderId,
            @RequestParam String reason) {
        return ApiResult.success(refundService.createRefund(orderId, userId, reason));
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/order/{orderId}")
    public ApiResult<Payment> getPaymentByOrder(@PathVariable Long orderId) {
        return ApiResult.success(paymentService.getPaymentByOrderId(orderId));
    }
}
