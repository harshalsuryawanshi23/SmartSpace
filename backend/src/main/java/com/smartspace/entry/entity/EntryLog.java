package com.smartspace.entry.entity;

import com.smartspace.booking.entity.Booking;
import com.smartspace.listing.entity.Hall;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_event_id", length = 36, unique = true, columnDefinition = "CHAR(36)")
    private String clientEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id")
    private EntryCredential credential;

    @Column(name = "watchman_user_id")
    private Long watchmanUserId;

    @Column(name = "event_type", nullable = false, columnDefinition = "ENUM('SCAN','OTP_SENT','OTP_VERIFIED','CHECK_IN','REENTRY','CHECK_OUT','HEADCOUNT','CAPACITY_ALERT','WRAP_UP_ALERT','OVERSTAY_ALERT','DECORATOR_IN','DECORATOR_OUT','SYNC_CONFLICT')")
    private String eventType;

    @Column(columnDefinition = "ENUM('GO','HOLD','STOP')")
    private String verdict; // GO, HOLD, STOP

    @Column(name = "reason_code", length = 40)
    private String reasonCode;

    @Column(name = "identity_method", columnDefinition = "ENUM('OTP','MANUAL_OFFLINE','NONE')")
    private String identityMethod; // OTP, MANUAL_OFFLINE, NONE

    @Column(nullable = false)
    private Boolean offline;

    @Column(columnDefinition = "SMALLINT UNSIGNED")
    private Integer headcount;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(columnDefinition = "JSON")
    private String meta;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    public void prePersist() {
        if (this.offline == null) {
            this.offline = false;
        }
    }
}
