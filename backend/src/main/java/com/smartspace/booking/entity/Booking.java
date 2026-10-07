package com.smartspace.booking.entity;

import com.smartspace.listing.entity.Hall;
import com.smartspace.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true, length = 36, columnDefinition = "CHAR(36)")
    private String publicId = UUID.randomUUID().toString();

    @Column(name = "booking_ref", nullable = false, updatable = false, unique = true, length = 20)
    private String bookingRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_user_id", nullable = false)
    private User renter;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private BookingEventType eventType;

    @Column(name = "event_title", nullable = false, length = 160)
    private String eventTitle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "theme_tags", columnDefinition = "json")
    private String themeTags;

    @Column(name = "guest_count", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer guestCount;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    @Column(name = "lock_expires_at")
    private Instant lockExpiresAt;

    @Column(name = "price_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceBase;

    @Column(name = "price_member_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMemberDiscount = BigDecimal.ZERO;

    @Column(name = "price_platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePlatformFee = BigDecimal.ZERO;

    @Column(name = "price_tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceTax = BigDecimal.ZERO;

    @Column(name = "price_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceTotal;

    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currency = "INR";

    @Column(name = "is_member_booking", nullable = false)
    private boolean isMemberBooking;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false)
    private CancellationPolicy cancellationPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private ActorType cancelledBy;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "checked_out_at")
    private Instant checkedOutAt;

    @Column(name = "arrived_headcount", columnDefinition = "SMALLINT UNSIGNED")
    private Integer arrivedHeadcount;

    @Column(name = "peak_headcount", columnDefinition = "SMALLINT UNSIGNED")
    private Integer peakHeadcount;

    @Column(name = "overstay_minutes", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer overstayMinutes = 0;

    @Column(name = "last_overstay_alert_at")
    private Instant lastOverstayAlertAt;

    @Column(name = "dispute_open", nullable = false)
    private boolean disputeOpen = false;

    @Column(name = "rating_window_closes_at")
    private Instant ratingWindowClosesAt;

    @Version
    @Column(nullable = false)
    private Integer version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
