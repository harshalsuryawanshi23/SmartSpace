package com.smartspace.trust.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trust_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TrustScoreId.class)
public class TrustScore {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, columnDefinition = "ENUM('RESIDENT','HALL','DECORATOR')")
    private Rating.SubjectType subjectType;

    @Id
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge", nullable = false, columnDefinition = "ENUM('NEW','STANDARD','TRUSTED','WATCH')")
    private Badge badge;

    @Column(name = "verified_stays", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer verifiedStays = 0;

    @Column(name = "components", columnDefinition = "JSON", nullable = false)
    private String components;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    public enum Badge {
        NEW, STANDARD, TRUSTED, WATCH
    }
}
