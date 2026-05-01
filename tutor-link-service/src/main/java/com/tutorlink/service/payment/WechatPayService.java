package com.tutorlink.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务
 * 封装微信支付 API 调用 (JSAPI 预付单、查询、退款)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayService {

    @Value("${wechat.pay.mch-id:}")
    private String mchId;

    @Value("${wechat.pay.api-key:}")
    private String apiKey;

    @Value("${wechat.pay.cert-path:}")
    private String certPath;

    @Value("${wechat.pay.notify-url:}")
    private String notifyUrl;

    @Value("${wechat.miniapp.appid:}")
    private String appId;

    private final ObjectMapper objectMapper;

    /**
     * 创建 JSAPI 预付单
     * 返回给前端用于调起微信支付的参数
     */
    public Map<String, String> createJsapiPrepay(String orderNo, int totalFeeCents,
                                                   String description, String openid) {
        // TODO: 实际调用微信支付 V3 API 创建预付单
        // 1. 构建请求参数 (appid, mchid, description, out_trade_no, notify_url, amount)
        // 2. 签名并发送请求到 https://api.mch.weixin.qq.com/v3/pay/transactions/native
        // 3. 获取 prepay_id
        // 4. 构建前端调起支付所需参数 (timeStamp, nonceStr, package, signType, paySign)

        log.info("Creating JSAPI prepay: orderNo={}, totalFee={}, openid={}", orderNo, totalFeeCents, openid);

        // 模拟返回 (开发阶段)
        Map<String, String> result = new HashMap<>();
        result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        result.put("nonceStr", java.util.UUID.randomUUID().toString().replace("-", ""));
        result.put("package", "prepay_id=wx" + System.currentTimeMillis());
        result.put("signType", "RSA");
        result.put("paySign", "mock_sign_for_dev");
        return result;
    }

    /**
     * 查询支付状态
     */
    public Map<String, Object> queryOrder(String outTradeNo) {
        // TODO: 调用微信支付查询 API
        // GET https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{out_trade_no}?mchid={mchid}
        log.info("Querying payment status: outTradeNo={}", outTradeNo);
        return Map.of("trade_state", "NOTPAY");
    }

    /**
     * 申请退款
     */
    public Map<String, Object> createRefund(String outTradeNo, String outRefundNo,
                                              int refundAmount, int totalAmount, String reason) {
        // TODO: 调用微信支付退款 API
        // POST https://api.mch.weixin.qq.com/v3/refund/domestic/refunds
        log.info("Creating refund: outTradeNo={}, refundAmount={}, reason={}", outTradeNo, refundAmount, reason);

        return Map.of(
                "refund_id", "wx_refund_" + System.currentTimeMillis(),
                "status", "PROCESSING"
        );
    }

    /**
     * 验证微信支付回调签名
     */
    public boolean verifyCallbackSignature(String body, String signature, String timestamp, String nonce) {
        // TODO: 使用微信平台证书验证回调签名
        return true;
    }
}
