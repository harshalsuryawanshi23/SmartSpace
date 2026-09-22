package com.smartspace.payment.controller;

import com.smartspace.payment.entity.Payment;
import com.smartspace.payment.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        Payment payment = paymentService.createOrder(request.getBookingRef(), request.getAmount());
        return ResponseEntity.ok(new OrderResponse(
                payment.getProviderOrderId(), 
                payment.getProvider(),
                payment.getAmount()
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verifyPayment(@RequestBody VerifyRequest request) {
        Payment payment = paymentService.verifyPayment(
                request.getOrderId(), 
                request.getPaymentId(), 
                request.getSignature()
        );
        return ResponseEntity.ok(new VerifyResponse(payment.getStatus().name()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String rawPayload) {
        // Normally, we'd verify the signature with our webhook secret here
        // If valid, and event is payment.captured, we'd check if we need to verifyPayment.
        // For brevity in this exercise, we will just return OK to acknowledge.
        return ResponseEntity.ok().build();
    }

    // --- DTOs ---

    @Data
    public static class OrderRequest {
        private String bookingRef;
        private BigDecimal amount;
    }

    @Data
    public static class OrderResponse {
        private final String orderId;
        private final String provider;
        private final BigDecimal amount;
    }

    @Data
    public static class VerifyRequest {
        private String orderId;
        private String paymentId;
        private String signature;
    }

    @Data
    public static class VerifyResponse {
        private final String status;
    }
}
