package com.smartspace.dispute.entity;

import com.smartspace.booking.entity.Booking;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "raised_by", nullable = false)
    private Long raisedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "against_side", nullable = false, columnDefinition = "ENUM('RENTER','HALL_SIDE')")
    private AgainstSide againstSide;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "ENUM('DAMAGE','OVERSTAY','NO_ACCESS','MISREPRESENTED_LISTING','CLEANLINESS','OTHER')")
    private Category category;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "claimed_amount", precision = 10, scale = 2)
    private BigDecimal claimedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('OPEN','UNDER_REVIEW','RESOLVED_FOR_RAISER','RESOLVED_AGAINST_RAISER','PARTIAL','WITHDRAWN')")
    private Status status = Status.OPEN;

    @Column(name = "resolution_note", length = 2000)
    private String resolutionNote;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum AgainstSide {
        RENTER, HALL_SIDE
    }

    public enum Category {
        DAMAGE, OVERSTAY, NO_ACCESS, MISREPRESENTED_LISTING, CLEANLINESS, OTHER
    }

    public enum Status {
        OPEN, UNDER_REVIEW, RESOLVED_FOR_RAISER, RESOLVED_AGAINST_RAISER, PARTIAL, WITHDRAWN
    }
}
