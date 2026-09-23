package com.smartspace.decorator.entity;

import com.smartspace.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "decorators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decorator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal lat;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal lng;

    @Column(name = "service_radius_km", nullable = false, precision = 5, scale = 2)
    private BigDecimal serviceRadiusKm;

    @Column(name = "portfolio_urls", columnDefinition = "JSON")
    @Convert(converter = com.smartspace.common.converter.JsonStringListConverter.class)
    private List<String> portfolioUrls;

    @Column(name = "trust_score")
    private Integer trustScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecoratorStatus status = DecoratorStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum DecoratorStatus {
        PENDING, APPROVED, REJECTED, INACTIVE
    }
}
