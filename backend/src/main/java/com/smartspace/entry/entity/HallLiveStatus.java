package com.smartspace.entry.entity;

import com.smartspace.booking.entity.Booking;
import com.smartspace.hall.entity.Hall;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hall_live_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallLiveStatus {

    @Id
    private Long hallId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "hall_id")
    private Hall hall;

    @Column(nullable = false)
    private String status; // FREE, OCCUPIED, CLEANING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_booking_id")
    private Booking currentBooking;

    @Column(name = "current_headcount", nullable = false)
    private Integer currentHeadcount;

    @Column(name = "capacity_alert_level", length = 20)
    private String capacityAlertLevel;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "FREE";
        }
        if (this.currentHeadcount == null) {
            this.currentHeadcount = 0;
        }
    }
}
