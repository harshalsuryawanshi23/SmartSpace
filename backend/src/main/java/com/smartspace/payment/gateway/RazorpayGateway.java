package com.smartspace.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "razorpay")
public class RazorpayGateway implements PaymentGateway {

    @Value("${app.payment.razorpay.key-id:}")
    private String keyId;

    @Value("${app.payment.razorpay.key-secret:}")
    private String keySecret;

    @Override
    public String createOrder(BigDecimal amount, String receiptId) {
        // Normally calls Razorpay SDK: razorpayClient.orders.create(...)
        // Since we don't have the SDK in pom.xml, this acts as a stub for Razorpay 
        log.info("Razorpay createOrder stub: amt={}, receipt={}", amount, receiptId);
        return "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);

            byte[] hash = sha256HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = bytesToHex(hash);

            // Constant-time string comparison
            return MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error verifying Razorpay signature", e);
            return false;
        }
    }

    @Override
    public String initiateRefund(String paymentId, BigDecimal amount) {
        // Normally calls razorpayClient.payments.refund(...)
        log.info("Razorpay initiateRefund stub: payId={}, amt={}", paymentId, amount);
        return "rfnd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }

    @Override
    public String getProviderName() {
        return "RAZORPAY";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
