package com.smartspace.trust.entity;

import com.smartspace.booking.entity.Booking;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "rater_user_id", nullable = false)
    private Long raterUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rater_side", nullable = false, columnDefinition = "ENUM('RENTER','HALL_SIDE')")
    private RaterSide raterSide;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, columnDefinition = "ENUM('HALL','RENTER','DECORATOR')")
    private SubjectType subjectType;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "stars", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer stars;

    @Column(name = "dimensions", columnDefinition = "JSON")
    private String dimensions;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "weight", nullable = false, precision = 3, scale = 2)
    private BigDecimal weight = new BigDecimal("1.00");

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum RaterSide {
        RENTER, HALL_SIDE
    }

    public enum SubjectType {
        HALL, RENTER, DECORATOR
    }
}
