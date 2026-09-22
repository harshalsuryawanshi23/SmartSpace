package com.smartspace.entry.entity;

import com.smartspace.booking.entity.Booking;
import com.smartspace.identity.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "handover_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandoverReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false)
    private HandoverPhase phase;

    @Column(name = "checklist", columnDefinition = "JSON", nullable = false)
    private String checklist;

    @Column(name = "checklist_score", nullable = false, precision = 4, scale = 3)
    private BigDecimal checklistScore;

    @Column(name = "notes", length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public enum HandoverPhase {
        BEFORE, AFTER
    }
}
