package com.smartspace.payment.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockGateway implements PaymentGateway {

    @Override
    public String createOrder(BigDecimal amount, String receiptId) {
        return "mock_order_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        // In mock, signature = "mock_valid_signature" is considered valid
        return "mock_valid_signature".equals(signature) || signature.startsWith("mock_");
    }

    @Override
    public String initiateRefund(String paymentId, BigDecimal amount) {
        return "mock_refund_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }
}
