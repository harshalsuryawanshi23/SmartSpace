package com.smartspace.booking.entity;

import com.smartspace.core.entity.BaseEntity;
import com.smartspace.listing.entity.Hall;
import com.smartspace.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Entity
@Table(name = "waitlist_entries")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WaitlistEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "event_type")
    private String eventType;

    @Column(nullable = false)
    private String status; // WAITING, OFFERED, CONVERTED, EXPIRED, CANCELLED
}
