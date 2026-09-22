package com.smartspace.decorator.entity;

import com.smartspace.booking.entity.Booking;
import com.smartspace.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "decorator_enquiries")
@Getter
@Setter
public class DecoratorEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decorator_id", nullable = false)
    private Decorator decorator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private DecoratorPackage decoratorPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_user_id", nullable = false)
    private User renterUser;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "match_score", precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "match_explanation", columnDefinition = "JSON")
    private String matchExplanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnquiryStatus status = EnquiryStatus.SENT;

    @Column(name = "quoted_price", precision = 10, scale = 2)
    private BigDecimal quotedPrice;

    @Column(name = "setup_window_start")
    private LocalDateTime setupWindowStart;

    @Column(name = "setup_window_end")
    private LocalDateTime setupWindowEnd;

    @Column(name = "teardown_window_end")
    private LocalDateTime teardownWindowEnd;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum EnquiryStatus {
        SENT, ACCEPTED, DECLINED, EXPIRED, CONFIRMED_BY_RENTER, CANCELLED
    }
}
