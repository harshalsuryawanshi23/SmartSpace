package com.smartspace.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "societies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Society {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false, length = 36, columnDefinition = "CHAR(36)")
    private String publicId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String locality;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 6, columnDefinition = "CHAR(6)")
    private String pincode;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal lng;

    @Column(name = "manager_user_id", nullable = false)
    private Long managerUserId;

    @Column(name = "registration_doc_path")
    private String registrationDocPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, columnDefinition = "ENUM('PENDING','APPROVED','REJECTED')")
    private VerificationStatus verificationStatus;

    @Column(name = "rejection_reason")
    private String rejectionReason;

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
        if (this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.PENDING;
        }
    }
}
