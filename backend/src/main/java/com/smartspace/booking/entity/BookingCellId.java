package com.smartspace.booking.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCellId implements Serializable {
    private Long hallId;
    private LocalDateTime cellStart;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingCellId that = (BookingCellId) o;
        return Objects.equals(hallId, that.hallId) &&
                Objects.equals(cellStart, that.cellStart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hallId, cellStart);
    }
}
