package com.smartspace.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_cells")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCell {

    @EmbeddedId
    private BookingCellId id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;
}
