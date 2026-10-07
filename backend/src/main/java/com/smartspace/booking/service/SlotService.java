package com.smartspace.booking.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.CellType;
import com.smartspace.booking.exception.SlotUnavailableException;
import com.smartspace.listing.entity.Hall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotService {

    private final JdbcTemplate jdbc;
    private final LockExpiryService lockExpiryService;

    /**
     * Attempts to reserve the given time slot for a booking by creating cells.
     * Throws SlotUnavailableException if any 30-minute cell is already taken.
     */
    public void reserve(Hall hall, Instant start, Instant end, Booking booking) {
        // 1. Lazy cleanup of expired locks
        Instant endWithBuffer = end.plus(hall.getBufferAfterMinutes(), ChronoUnit.MINUTES);
        lockExpiryService.releaseExpiredOverlapping(hall.getId(), start, endWithBuffer);

        // 2. Build cells
        List<Object[]> batchArgs = new ArrayList<>();
        
        // BOOKED cells
        Instant current = start;
        while (current.isBefore(end)) {
            batchArgs.add(new Object[]{hall.getId(), Timestamp.from(current), booking.getId(), CellType.BOOKED.name()});
            current = current.plus(30, ChronoUnit.MINUTES);
        }

        // BUFFER cells
        current = end;
        while (current.isBefore(endWithBuffer)) {
            batchArgs.add(new Object[]{hall.getId(), Timestamp.from(current), booking.getId(), CellType.BUFFER.name()});
            current = current.plus(30, ChronoUnit.MINUTES);
        }

        // 3. Batch insert
        String sql = "INSERT INTO booking_cells (hall_id, cell_start, booking_id, cell_type) VALUES (?, ?, ?, ?)";
        try {
            jdbc.batchUpdate(sql, batchArgs);
        } catch (DuplicateKeyException e) {
            log.warn("SlotUnavailableException: Cell already taken for hall {} between {} and {}", hall.getId(), start, endWithBuffer);
            throw new SlotUnavailableException();
        }
    }
    public boolean isSlotAvailable(Long hallId, Instant start, Instant end) {
        String sql = "SELECT COUNT(*) FROM booking_cells WHERE hall_id = ? AND cell_start >= ? AND cell_start < ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, hallId, Timestamp.from(start), Timestamp.from(end));
        return count != null && count == 0;
    }
}
