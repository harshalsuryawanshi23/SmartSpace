package com.smartspace.decorator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "decorator_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecoratorPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decorator_id", nullable = false)
    private Decorator decorator;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_types", nullable = false, columnDefinition = "JSON")
    @Convert(converter = com.smartspace.common.converter.JsonStringListConverter.class)
    private List<String> eventTypes;

    @Column(name = "theme_tags", nullable = false, columnDefinition = "JSON")
    @Convert(converter = com.smartspace.common.converter.JsonStringListConverter.class)
    private List<String> themeTags;

    @Column(name = "layout_types", nullable = false, columnDefinition = "JSON")
    @Convert(converter = com.smartspace.common.converter.JsonStringListConverter.class)
    private List<String> layoutTypes;

    @Column(name = "min_capacity", nullable = false)
    private Integer minCapacity;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "price_per_guest", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerGuest = BigDecimal.ZERO;

    @Column(name = "setup_minutes", nullable = false)
    private Integer setupMinutes;

    @Column(name = "teardown_minutes", nullable = false)
    private Integer teardownMinutes;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
