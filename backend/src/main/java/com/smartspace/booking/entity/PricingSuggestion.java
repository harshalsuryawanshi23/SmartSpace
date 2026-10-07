package com.smartspace.booking.entity;

import com.smartspace.core.entity.BaseEntity;
import com.smartspace.listing.entity.Hall;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pricing_suggestions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PricingSuggestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "start_hour", nullable = false)
    private Integer startHour;

    @Column(name = "end_hour", nullable = false)
    private Integer endHour;

    @Column(name = "suggestion_type", nullable = false)
    private String suggestionType; // SURGE, DISCOUNT

    @Column(nullable = false)
    private Integer percentage;

    @Column(name = "rationale_json", columnDefinition = "jsonb")
    private String rationaleJson;

    @Column(nullable = false)
    private String status; // PENDING, APPLIED, DISMISSED
}
