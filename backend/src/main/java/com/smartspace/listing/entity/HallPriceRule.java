package com.smartspace.listing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "hall_price_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallPriceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hall_id", nullable = false)
    private Long hallId;

    @Column(nullable = false, length = 60)
    private String label;

    @Column(name = "days_mask", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer daysMask;

    @Column(name = "from_time", nullable = false)
    private LocalTime fromTime;

    @Column(name = "to_time", nullable = false)
    private LocalTime toTime;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private Integer priority;

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    public void prePersist() {
        if (this.priority == null) this.priority = 0;
        if (this.active == null) this.active = true;
    }
}
