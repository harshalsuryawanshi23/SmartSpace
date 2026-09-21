package com.smartspace.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "halls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false, length = 36)
    private String publicId;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String locality;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal lng;

    @Column(name = "capacity_seated", nullable = false)
    private Integer capacitySeated;

    @Column(name = "capacity_standing", nullable = false)
    private Integer capacityStanding;

    @Column(name = "area_sqft")
    private Integer areaSqft;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type", nullable = false)
    private LayoutType layoutType;

    @Column(name = "ceiling_height_ft", precision = 4, scale = 1)
    private BigDecimal ceilingHeightFt;

    @Column(nullable = false)
    private Boolean indoor;

    @Column(name = "has_ac", nullable = false)
    private Boolean hasAc;

    @Column(name = "has_parking", nullable = false)
    private Boolean hasParking;

    @Column(name = "has_kitchen", nullable = false)
    private Boolean hasKitchen;

    @Column(name = "has_stage", nullable = false)
    private Boolean hasStage;

    @Column(name = "has_power_backup", nullable = false)
    private Boolean hasPowerBackup;

    @Column(name = "has_washroom", nullable = false)
    private Boolean hasWashroom;

    @Column(name = "power_points")
    private Integer powerPoints;

    @Column(name = "rules_text", columnDefinition = "TEXT")
    private String rulesText;

    @Column(name = "base_price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerHour;

    @Column(name = "min_slot_minutes", nullable = false)
    private Integer minSlotMinutes;

    @Column(name = "max_slot_minutes", nullable = false)
    private Integer maxSlotMinutes;

    @Column(name = "buffer_after_minutes", nullable = false)
    private Integer bufferAfterMinutes;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(name = "latest_end_time")
    private LocalTime latestEndTime;

    @Column(name = "advance_days_public", nullable = false)
    private Integer advanceDaysPublic;

    @Column(name = "advance_days_member", nullable = false)
    private Integer advanceDaysMember;

    @Column(name = "member_discount_percent", nullable = false, precision = 4, scale = 1)
    private BigDecimal memberDiscountPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false)
    private CancellationPolicy cancellationPolicy;

    @Column(name = "overstay_fee_per_15min", nullable = false, precision = 10, scale = 2)
    private BigDecimal overstayFeePer15Min;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HallStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @Column(name = "trust_score", precision = 5, scale = 2)
    private BigDecimal trustScore;

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
        if (this.layoutType == null) {
            this.layoutType = LayoutType.OPEN_HALL;
        }
        if (this.indoor == null) this.indoor = true;
        if (this.hasAc == null) this.hasAc = false;
        if (this.hasParking == null) this.hasParking = false;
        if (this.hasKitchen == null) this.hasKitchen = false;
        if (this.hasStage == null) this.hasStage = false;
        if (this.hasPowerBackup == null) this.hasPowerBackup = false;
        if (this.hasWashroom == null) this.hasWashroom = true;
        
        if (this.minSlotMinutes == null) this.minSlotMinutes = 120;
        if (this.maxSlotMinutes == null) this.maxSlotMinutes = 240;
        if (this.bufferAfterMinutes == null) this.bufferAfterMinutes = 30;
        if (this.advanceDaysPublic == null) this.advanceDaysPublic = 30;
        if (this.advanceDaysMember == null) this.advanceDaysMember = 60;
        if (this.memberDiscountPercent == null) this.memberDiscountPercent = BigDecimal.ZERO;
        if (this.cancellationPolicy == null) this.cancellationPolicy = CancellationPolicy.MODERATE;
        if (this.overstayFeePer15Min == null) this.overstayFeePer15Min = BigDecimal.ZERO;
        
        if (this.status == null) this.status = HallStatus.DRAFT;
        if (this.ratingAvg == null) this.ratingAvg = BigDecimal.ZERO;
        if (this.ratingCount == null) this.ratingCount = 0;
    }
}
