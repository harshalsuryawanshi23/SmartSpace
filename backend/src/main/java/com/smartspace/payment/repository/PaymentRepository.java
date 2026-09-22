package com.smartspace.payment.repository;

import com.smartspace.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByProviderAndProviderOrderId(String provider, String providerOrderId);
    Optional<Payment> findByProviderAndProviderPaymentId(String provider, String providerPaymentId);
    Optional<Payment> findByBookingId(Long bookingId);
}
