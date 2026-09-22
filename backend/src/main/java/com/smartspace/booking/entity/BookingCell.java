package com.smartspace.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "booking_cells")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BookingCell {

    @EmbeddedId
    private BookingCellId id = new BookingCellId();

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cell_type", nullable = false)
    private CellType cellType = CellType.BOOKED;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public void setHallId(Long hallId) {
        this.id.setHallId(hallId);
    }

    public void setCellStart(Instant cellStart) {
        this.id.setCellStart(cellStart);
    }

    public Long getHallId() {
        return id.getHallId();
    }

    public Instant getCellStart() {
        return id.getCellStart();
    }
}
