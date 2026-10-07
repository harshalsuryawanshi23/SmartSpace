package com.smartspace.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "booking_cohosts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCohost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "phone", length = 16)
    private String phone;

    @Column(name = "share_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal shareAmount;

    @Column(name = "pay_token", nullable = false, unique = true, length = 32, columnDefinition = "CHAR(32)")
    private String payToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CohostStatus status = CohostStatus.INVITED;

    @Column(name = "paid_at")
    private Instant paidAt;

    public enum CohostStatus {
        INVITED,
        PAID,
        EXPIRED
    }
}
