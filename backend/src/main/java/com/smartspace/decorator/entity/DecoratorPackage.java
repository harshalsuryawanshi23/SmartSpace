package com.smartspace.decorator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "decorator_packages")
@Getter
@Setter
public class DecoratorPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decorator_id", nullable = false)
    private Decorator decorator;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_types", nullable = false, columnDefinition = "JSON")
    private String eventTypes; // JSON array, e.g. ["BIRTHDAY","BABY_SHOWER"] or ["ANY"]

    @Column(name = "theme_tags", nullable = false, columnDefinition = "JSON")
    private String themeTags; // JSON array, e.g. ["balloon","floral","kids","traditional"]

    @Column(name = "layout_types", nullable = false, columnDefinition = "JSON")
    private String layoutTypes; // JSON array, e.g. ["OPEN_HALL","STAGE_HALL"] or ["ANY"]

    @Column(name = "min_capacity", nullable = false)
    private Integer minCapacity;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "price_per_guest", nullable = false, precision = 8, scale = 2)
    private BigDecimal pricePerGuest = BigDecimal.ZERO;

    @Column(name = "setup_minutes", nullable = false)
    private Integer setupMinutes = 60;

    @Column(name = "teardown_minutes", nullable = false)
    private Integer teardownMinutes = 30;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
