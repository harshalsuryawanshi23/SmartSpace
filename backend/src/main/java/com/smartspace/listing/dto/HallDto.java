package com.smartspace.listing.dto;

import com.smartspace.listing.entity.CancellationPolicy;
import com.smartspace.listing.entity.HallStatus;
import com.smartspace.listing.entity.LayoutType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class HallDto {
    private String id;
    private String societyId;
    private String name;
    private String description;
    private String addressLine;
    private String locality;
    private String city;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer capacitySeated;
    private Integer capacityStanding;
    private Integer areaSqft;
    private LayoutType layoutType;
    private BigDecimal ceilingHeightFt;
    private Boolean indoor;
    private Boolean hasAc;
    private Boolean hasParking;
    private Boolean hasKitchen;
    private Boolean hasStage;
    private Boolean hasPowerBackup;
    private Boolean hasWashroom;
    private Integer powerPoints;
    private String rulesText;
    private BigDecimal basePricePerHour;
    private Integer minSlotMinutes;
    private Integer maxSlotMinutes;
    private Integer bufferAfterMinutes;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private LocalTime latestEndTime;
    private Integer advanceDaysPublic;
    private Integer advanceDaysMember;
    private BigDecimal memberDiscountPercent;
    private CancellationPolicy cancellationPolicy;
    private BigDecimal overstayFeePer15Min;
    private HallStatus status;
    private String rejectionReason;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private BigDecimal trustScore;
    private LocalDateTime createdAt;
}
