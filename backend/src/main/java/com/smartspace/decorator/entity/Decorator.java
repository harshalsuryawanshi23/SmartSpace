package com.smartspace.decorator.entity;

import com.smartspace.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "decorators")
@Getter
@Setter
public class Decorator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal baseLat;

    @Column(name = "base_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal baseLng;

    @Column(name = "service_radius_km", nullable = false, precision = 5, scale = 1)
    private BigDecimal serviceRadiusKm = new BigDecimal("10.0");

    @Column(name = "portfolio_paths", columnDefinition = "JSON")
    private String portfolioPaths;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    @Column(name = "trust_score", precision = 5, scale = 2)
    private BigDecimal trustScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum VerificationStatus {
        PENDING, APPROVED, REJECTED, SUSPENDED
    }
}
