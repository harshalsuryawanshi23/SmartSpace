package com.smartspace.payment.entity;

import com.smartspace.booking.entity.Booking;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false, length = 36)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 20)
    private String provider; // MOCK | RAZORPAY

    @Column(name = "provider_order_id", nullable = false, length = 80)
    private String providerOrderId;

    @Column(name = "provider_payment_id", length = 80)
    private String providerPaymentId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "signature_verified", nullable = false)
    private Boolean signatureVerified;

    @Column(name = "failure_reason", length = 160)
    private String failureReason;

    @Column(name = "raw_payload", columnDefinition = "JSON")
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = java.util.UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = PaymentStatus.CREATED;
        }
        if (this.currency == null) {
            this.currency = "INR";
        }
        if (this.signatureVerified == null) {
            this.signatureVerified = false;
        }
    }
}
