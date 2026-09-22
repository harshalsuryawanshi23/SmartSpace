package com.smartspace.payment.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.service.BookingStateMachine;
import com.smartspace.payment.entity.Payment;
import com.smartspace.payment.entity.PaymentStatus;
import com.smartspace.payment.gateway.PaymentGateway;
import com.smartspace.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final com.smartspace.payment.repository.RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final PaymentGateway paymentGateway;
    private final BookingStateMachine bookingStateMachine;
    private final com.smartspace.entry.service.QrTokenService qrTokenService;

    @Transactional
    public Payment createOrder(String bookingRef, BigDecimal amount) {
        Booking booking = bookingRepository.findByBookingRef(bookingRef)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Booking is not in PENDING_PAYMENT state");
        }

        if (amount.compareTo(booking.getTotalPrice()) != 0) {
            throw new IllegalArgumentException("Amount mismatch. Expected: " + booking.getTotalPrice());
        }

        // Idempotency check: if a created payment already exists for this booking, return it
        Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId())
                .filter(p -> p.getStatus() == PaymentStatus.CREATED);
        
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        String orderId = paymentGateway.createOrder(amount, bookingRef);

        Payment payment = Payment.builder()
                .booking(booking)
                .provider(paymentGateway.getProviderName())
                .providerOrderId(orderId)
                .amount(amount)
                .currency("INR")
                .status(PaymentStatus.CREATED)
                .signatureVerified(false)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment verifyPayment(String orderId, String paymentId, String signature) {
        Payment payment = paymentRepository.findByProviderAndProviderOrderId(paymentGateway.getProviderName(), orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            // Already verified (e.g. by webhook earlier)
            return payment;
        }

        boolean isValid = paymentGateway.verifySignature(orderId, paymentId, signature);
        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);
            throw new IllegalArgumentException("Invalid payment signature");
        }

        payment.setProviderPaymentId(paymentId);
        payment.setSignatureVerified(true);
        payment.setStatus(PaymentStatus.CAPTURED);
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            // Transition booking to CONFIRMED
            bookingStateMachine.transition(booking, BookingStatus.CONFIRMED, "system", "Payment captured");
            bookingRepository.save(booking);
            
            // Issue QR Credential
            String qrToken = qrTokenService.issueHolderCredential(booking);
            log.info("Issued QR token for booking {}: {}", booking.getBookingRef(), qrToken);
        } else if (booking.getStatus() == BookingStatus.CANCELLED_AUTO) {
            // Late payment race condition! 
            // The booking lock expired and it auto-cancelled, but the payment succeeded just now.
            log.warn("Late payment race condition for booking {}. Issuing refund.", booking.getBookingRef());
            // Need to refund this payment!
            initiateRefund(payment, payment.getAmount(), com.smartspace.payment.entity.RefundReason.ADMIN);
        } else {
            log.warn("Payment verified but booking {} is in state {}", booking.getBookingRef(), booking.getStatus());
        }

        return payment;
    }

    @Transactional
    public void initiateRefund(Payment payment, BigDecimal amount, com.smartspace.payment.entity.RefundReason reason) {
        String refundId = paymentGateway.initiateRefund(payment.getProviderPaymentId(), amount);
        
        // Save Refund entity
        com.smartspace.payment.entity.Refund refund = com.smartspace.payment.entity.Refund.builder()
                .payment(payment)
                .booking(payment.getBooking())
                .amount(amount)
                .reason(reason)
                .providerRefundId(refundId)
                .status(com.smartspace.payment.entity.RefundStatus.PROCESSED)
                .build();
        
        refundRepository.save(refund);
    }

    @Transactional
    public void initiateRefundForBooking(Long bookingId, BigDecimal amount, com.smartspace.payment.entity.RefundReason reason) {
        Optional<Payment> capturedPayment = paymentRepository.findByBookingId(bookingId)
                .filter(p -> p.getStatus() == PaymentStatus.CAPTURED);
                
        if (capturedPayment.isPresent()) {
            initiateRefund(capturedPayment.get(), amount, reason);
        } else {
            log.warn("No CAPTURED payment found for booking {}, refund of {} skipped", bookingId, amount);
        }
    }
}
