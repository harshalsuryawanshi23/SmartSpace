package com.smartspace.listing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hall_blackouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallBlackout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hall_id", nullable = false)
    private Long hallId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(length = 160)
    private String reason;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;
}
