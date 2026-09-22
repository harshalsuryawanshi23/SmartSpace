package com.smartspace.booking.entity;

import com.smartspace.booking.entity.Booking;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "booking_events")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BookingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private HistoryEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    private ActorType actorType;

    @Column(name = "actor_id")
    private Long actorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String details;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
