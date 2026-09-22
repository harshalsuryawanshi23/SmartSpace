package com.smartspace.booking.service;

import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.booking.repository.BookingCellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockExpiryService {

    private final BookingRepository bookingRepository;
    private final BookingCellRepository bookingCellRepository;
    private final Clock clock;

    /**
     * Lazily releases expired locks that might block the currently requested window.
     */
    @Transactional
    public void releaseExpiredOverlapping(Long hallId, Instant windowStart, Instant windowEnd) {
        // Find any booking that has cells in the given window, is PENDING_PAYMENT, and is expired.
        // Actually, we can just run a general sweep of ALL expired bookings for this hall (or even globally).
        // For simplicity and to match the 'overlap' idea without complex joins, we can sweep the hall's expired bookings.
        List<com.smartspace.booking.entity.Booking> expired = bookingRepository.findExpiredLocks(BookingStatus.PENDING_PAYMENT, Instant.now(clock));
        for (com.smartspace.booking.entity.Booking b : expired) {
            if (b.getHall().getId().equals(hallId)) {
                log.info("Releasing expired lock for booking {}", b.getBookingRef());
                b.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(b);
                bookingCellRepository.deleteByBookingId(b.getId());
            }
        }
    }

    /**
     * Sweeps ALL expired locks across all halls (run by LockExpiryJob).
     */
    @Transactional
    public void sweepExpiredLocks() {
        List<com.smartspace.booking.entity.Booking> expired = bookingRepository.findExpiredLocks(BookingStatus.PENDING_PAYMENT, Instant.now(clock));
        for (com.smartspace.booking.entity.Booking b : expired) {
            log.info("Sweeping expired lock for booking {}", b.getBookingRef());
            b.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(b);
            bookingCellRepository.deleteByBookingId(b.getId());
        }
    }
}
