package com.smartspace.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    /**
     * Creates an order/intent on the payment provider.
     * @param amount The total amount to be paid.
     * @param receiptId A unique internal reference (e.g. Booking ID or Ref).
     * @return The provider's Order ID.
     */
    String createOrder(BigDecimal amount, String receiptId);

    /**
     * Verifies the signature of the payment confirmation sent by the client.
     * @param orderId Provider's Order ID
     * @param paymentId Provider's Payment ID
     * @param signature Cryptographic signature
     * @return true if valid
     */
    boolean verifySignature(String orderId, String paymentId, String signature);

    /**
     * Initiates a refund on the provider.
     * @param paymentId The original payment ID
     * @param amount The amount to refund
     * @return The provider's Refund ID
     */
    String initiateRefund(String paymentId, BigDecimal amount);
    
    /**
     * @return The provider identifier, e.g. "RAZORPAY" or "MOCK"
     */
    String getProviderName();
}
