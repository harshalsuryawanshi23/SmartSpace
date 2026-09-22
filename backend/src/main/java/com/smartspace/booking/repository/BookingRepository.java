package com.smartspace.booking.repository;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPublicId(String publicId);
    Optional<Booking> findByBookingRef(String bookingRef);

    List<Booking> findByRenterIdOrderByStartAtDesc(Long renterId);
    List<Booking> findByHallIdAndStartAtBetween(Long hallId, Instant start, Instant end);
    List<Booking> findByStatusAndStartAtBetween(BookingStatus status, Instant startMin, Instant startMax);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.lockExpiresAt < :now")
    List<Booking> findExpiredLocks(@Param("status") BookingStatus status, @Param("now") Instant now);
}
